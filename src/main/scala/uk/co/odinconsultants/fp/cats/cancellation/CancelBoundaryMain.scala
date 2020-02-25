package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object CancelBoundaryMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val x: IO[Unit] = IO {
      println("Sleeping...")
      Thread.sleep(2000)
      println("Slept")
    } >> IO.cancelBoundary

    x.map(_ => ExitCode.Success)
  }

}
