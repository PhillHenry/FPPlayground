package uk.co.odinconsultants.fp.cats.fs2.retries

import cats.ApplicativeError
import cats.effect.{ExitCase, ExitCode, IO, IOApp, Timer}
import cats.effect.concurrent.Ref
import cats.effect.concurrent.Deferred
import cats.implicits._

import scala.concurrent.duration._
import fs2.Stream

/**
 * @retriku fs2 handles only errors which are non-fatal, so you can simplify there already. Apart from that, the two
 * implementations have different behavior in cases where stream succeeds and later starts failing, as you can
 * see with this little test app (wrapWithRetries1 fails with an exception, wrapWithRetries2 doesn't):
 */
object RetryTestApp extends IOApp {

  def evil(counter: Ref[IO, Int]): IO[Unit] = counter.get.flatTap(c => IO(println(s"counter at $c"))) >>= {
    case i if i < 2 => counter.update(_ + 1)
    case _ => IO.raiseError(new Exception("BOOM"))
  }

  override def run(args: List[String]): IO[ExitCode] = (for {
    counter <- Stream.eval(Ref.of[IO, Int](0))
    _       <- wrapWithRetries1(1)(Stream.repeatEval(evil(counter)))
  } yield ExitCode.Success).compile.lastOrError


  type Retries = Int

  def delays[F[_]](retries: Retries): Stream[F, FiniteDuration] = Stream.constant(retries.second).take(3)

  def wrapWithRetries1[F[_] : Timer, O](retries: Retries)(stream: Stream[F, O])(implicit ae: ApplicativeError[F, Throwable], c: Stream.Compiler[F, F]): Stream[F, O] = {
    val eff = stream.compile.toList

    Stream.eval(eff)
      .attempts(delays(retries))
      .takeThrough(_.isLeft)
      .last
      .flatMap(el => Stream.emits(el.toSeq))
      .rethrow
      .flatMap(Stream.emits)
  }

  def wrapWithRetries2[F[_] : Timer, O](retries: Retries)(stream: Stream[F, O])(implicit ae: ApplicativeError[F, Throwable]): Stream[F, O] = {
    stream
      .attempts(delays(retries))
      .flatMap(_.fold(_ => Stream.empty, Stream.emit))
  }
}
