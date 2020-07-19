package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object CancelBoundaryMain extends IOApp {

  val sleeping: IO[Unit] = IO {
    println("Sleeping...")
    try {
      Thread.sleep(1000)
      println("Slept")
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }

  val sleepingWithBoundary = sleeping >> IO.cancelBoundary

  override def run(args: List[String]): IO[ExitCode] = {
    sleepingWithBoundary.map(_ => ExitCode.Success)
  }

}
