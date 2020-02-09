package uk.co.odinconsultants.fp.cancelling

import cats.effect.ExitCase.{Canceled, Completed, Error}
import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration._
import cats.implicits._

object CatsEffectsMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val workUncancellable: IO[Unit] = (IO.sleep(5.seconds) *>
      IO(println("Halfway")) *>
      IO.sleep(5.seconds) *>
      IO(println("Done"))).uncancelable

    val workBracketed = (IO.sleep(5.seconds) *>
      IO(println("Halfway")) *>
      IO.sleep(5.seconds) *>
      IO(println("Done")))
      .bracket(_ => IO.unit)(_ => IO.unit)

    doRace(workUncancellable) *> IO { println("\n\n") } *> doRace(workBracketed) *> ExitCode.Success.pure[IO]
  }

  private def doRace(work: IO[Unit]) = {
    (IO.race(work, (IO.sleep(4.seconds) *> IO(println("Finished")))))
      .guaranteeCase {
        case Completed => IO(println("Completed"))
        case Canceled => IO(println("Cancelled"))
        case Error(_) => IO(println("Error"))
      }
  }
}
