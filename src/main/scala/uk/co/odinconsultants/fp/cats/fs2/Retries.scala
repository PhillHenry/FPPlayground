package uk.co.odinconsultants.fp.cats.fs2

import cats.Applicative
import cats.effect.{ExitCode, IO, IOApp, Timer}
import fs2.{RaiseThrowable, Stream}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object Retries extends IOApp{

  /**
   * Retries `fo` on failure, returning a singleton stream with the
   * result of `fo` as soon as it succeeds.
   *
   * @param delay Duration of delay before the first retry
   * @param nextDelay Applied to the previous delay to compute the
   *                  next, e.g. to implement exponential backoff
   * @param maxAttempts Number of attempts before failing with the
   *                   latest error, if `fo` never succeeds
   * @param retriable Function to determine whether a failure is
   *                  retriable or not, defaults to retry every
   *                  `NonFatal`. A failed stream is immediately
   *                  returned when a non-retriable failure is
   *                  encountered
   */
  def retry[F[_]: Timer: RaiseThrowable, O](
                                             fo: F[Seq[O]],
                                             delay: FiniteDuration,
                                             nextDelay: FiniteDuration => FiniteDuration,
                                             maxAttempts: Int,
                                             retriable: Throwable => Boolean = scala.util.control.NonFatal.apply
                                           ): Stream[F, O] = {
    assert(maxAttempts > 0, s"maxAttempts should > 0, was $maxAttempts")

    val delays = Stream.unfold(delay)(d => Some(d -> nextDelay(d))).covary[F]

    Stream
      .evalSeq(fo)
      .attempts(delays)
      .take(maxAttempts)
      .takeThrough(_.fold(err => retriable(err), _ => false))
      .last
      .map(_.get)
      .rethrow
  }

  override def run(args: List[String]): IO[ExitCode] = {
    retry(IO { Seq(  new Throwable("Oops!"), println("hello") ) }, 1.second, x => 1.second, 10).compile.drain.map(_ => ExitCode.Success)
  }

  trait State {
    def running: Boolean
    def succeeded: Boolean
  }

  private def startJob: IO[Unit] = ???
  private def jobState: IO[State] = ???

  private def pollJob[T]: Stream[IO, State] =
    Stream
      .fixedDelay(1.minute)
      .evalMap(_ => jobState)
      .take(30)
      .takeThrough(_.running)

  private def checkReuslt(result: State): IO[Unit] =
    Applicative[IO].whenA(!result.succeeded) {
      IO.raiseError(new RuntimeException(s"Did not succeed: $result"))
    }

  def runJob: Stream[IO, Unit] =
    Stream
      .eval(startJob)
      .flatMap(_ => pollJob)
      .evalMap(checkReuslt)
}
