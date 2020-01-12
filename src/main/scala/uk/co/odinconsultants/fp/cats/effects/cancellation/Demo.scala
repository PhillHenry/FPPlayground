package uk.co.odinconsultants.fp.cats.effects.cancellation

import java.time.Instant
import java.time.Duration

import cats.effect.{Bracket, ExitCode, IO, IOApp}
import cats.effect.concurrent.{Deferred, Ref}

/**
 * Based on Jakub Kozłowski's code at:
 * @see https://scastie.scala-lang.org/cTQWIR8mRPOT0m4VPwRuRA
 */
object Demo extends IOApp {
  import cats.implicits._
  import scala.concurrent.duration.{Duration => _, _}

  //nested IO returns the amount of time passed since the first nested IO
  val getTime: IO[IO[Duration]] =
    Ref[IO].of(Option.empty[Instant]).map { started =>
      IO(Instant.now()).flatMap { now =>
        started.modify { prev =>
          val durationSince = prev match {
            case None        => Duration.ZERO
            case Some(start) => Duration.between(start, now)
          }

          (now.some, durationSince)
        }
      }
    }

  def putStrTimed(s: String)(time: IO[Duration]): IO[Unit] = time.flatMap { t =>
    IO(println(t.toMillis + "ms: " + s))
  }

  //from Bracket
  def uncancelableGood[F[_], A](fa: F[A])(implicit B: Bracket[F, Throwable]): F[A] =
    B.bracket(fa)(B.pure)(_ => B.unit)

  def uncancelableGoodAlternative[F[_]](fa: F[Unit])(implicit B: Bracket[F, Throwable]): F[Unit] =
    B.bracket(B.unit)(B.pure)(_ => fa)

  def uncancelableBad[A](ioa: IO[A]): IO[A] = ioa.uncancelable

  def example(tag: String)(mod: IO[Unit] => IO[Unit]): IO[Unit] =
    getTime.flatMap { time =>
      Deferred[IO, Unit].flatMap { promise =>
        val exec = Deferred[IO, Unit].flatMap { deff =>
          mod(deff.complete(()) *> promise.get).start.flatMap(deff.get *> _.cancel)
        } *>
          putStrTimed(s"$tag: Cancel completed")(time)

        val coordinate = IO.sleep(5.seconds) *> promise.complete(()) *> putStrTimed(s"$tag: Sleep completed")(time)

        exec &> coordinate
      }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    example("good")(uncancelableGood) *>
      example("good alt")(uncancelableGoodAlternative) *>
      example("bad")(uncancelableBad)
    }.as(ExitCode.Success)
}
