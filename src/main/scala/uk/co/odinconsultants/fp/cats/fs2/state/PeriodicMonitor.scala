package uk.co.odinconsultants.fp.cats.fs2.state

import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits._
import fs2.Stream

import scala.concurrent.duration._

object MyStateApp extends IOApp {

  val printlnIO = IO { println("IO: initial F") }

  val useFn: PeriodicMonitor[IO, Unit] => IO[Unit] = _.get.flatMap { _ =>
    println("inside flatmap")
    IO { println("IO: Looking at you, kid") }
  }

  val resource = PeriodicMonitor.create(printlnIO, 1 second)

  override def run(args: List[String]): IO[ExitCode] = {
    resource.use(useFn).as(ExitCode.Success)
  }
}


trait PeriodicMonitor[F[_], A] {
  def get: F[A]
}

/**
 * Mark Tomko @mtomko 04:17
 * Here it is with some error handling. I made a design choice which I think is debatable:
 * I didn't add an Error state to the State ADT. I think what this means is that if the monitor encounters an error,
 * it keeps working - there's no permanent transition from FirstCall or NextCalls to Error, and the stream continues
 * processing. If the caller of PeriodicMonitor.get notices an error (raised by the reader), then the caller can take
 * action on that. However, if I understand this code properly, it's possible the action can fail, be written, and then
 * recover and be overwritten with a subsequent successful value. I think this is not a bad property, although it
 * wasn't what I originally set out to do.
 */
object PeriodicMonitor {

  def create[F[_]: Concurrent: Timer, A](
                                          action: F[A],
                                          t: FiniteDuration
                                        ): Resource[F, PeriodicMonitor[F, A]] = {
    sealed trait State
    case class FirstCall(waitV: Deferred[F, Either[Throwable, A]])  extends State
    case class NextCalls(v:     Either[Throwable, A])               extends State

    val initial: F[Ref[F, State]] =
      for {
        d <- Deferred[F, Either[Throwable, A]]
        r <- Ref[F].of(FirstCall(d): State)
      } yield r

    Stream
      .eval(initial)
      .flatMap { state: Ref[F, State] =>

        val read = new PeriodicMonitor[F, A] {
          def get: F[A] = state.get.flatMap {
            case FirstCall(wait) => wait.get.rethrow
            case NextCalls(v)    => v.pure[F].rethrow
          }
        }

        val write: F[Unit] = action.attempt.flatMap { v =>
          val modified: F[F[Unit]] = state.modify {
            case FirstCall(wait) => NextCalls(v) -> wait.complete(v).void
            case NextCalls(_)    => NextCalls(v) -> ().pure[F]
          }
          modified.flatten
        }

        Stream.emit(read).concurrently(Stream.repeatEval(write).metered(t))
      }
      .compile
      .resource
      .lastOrError

  }
}

//Fabio Labella @SystemFw 00:27
/**
Fabio Labella @SystemFw 00:08
basically with this approach you want to model this sort of problems as a finite state machine
for this scenario you have two cases: the first call, where there is no value initially, and the subsequent calls where there is a new value that needs updating
the first scenario requires some waiting, in case the first get call arrives before the variable can be populated
waiting means having a deferred
so overall the state looks like this
sealed trait State[A]
case class FirstCall(wait: Deferred[F, A]) extends State[A]
case class NextCalls(v: A) extends State[A]
normally I put this as an inner class in the create method of the abstraction (I'll show you that in a bit), so that's
why there is no F[_] parameter, it's normally in the outer definition
 */
trait Thing[F[_], A] {
  def get: F[A]
}

object Thing {
  def create[F[_]: Concurrent: Timer, A](
                                          action: F[A],
                                          t: FiniteDuration
                                        ): Resource[F, Thing[F, A]] = {
    sealed trait State
    case class FirstCall(waitV: Deferred[F, A]) extends State
    case class NextCalls(v: A)                  extends State

    val initial = for {
      d <- Deferred[F, A]
      r <- Ref[F].of(FirstCall(d): State)
    } yield r

    Stream
      .eval(initial)
      .flatMap { state =>

        val read = new Thing[F, A] {
          def get: F[A] = state.get.flatMap {
            case FirstCall(wait)  => wait.get
            case NextCalls(v)     => v.pure[F]
          }
        }

        val write = action.flatMap { v =>
          state.modify {
            case FirstCall(wait)  => NextCalls(v) -> wait.complete(v).void
            case NextCalls(_)     => NextCalls(v) -> ().pure[F]
          }.flatten
        }

        Stream.emit(read).concurrently(Stream.repeatEval(write).metered(t))
      }
      .compile
      .resource
      .lastOrError

  }
}