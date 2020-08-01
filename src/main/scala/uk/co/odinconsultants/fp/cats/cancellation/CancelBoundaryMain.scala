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

  def sleepingWith(name: String): IO[Unit] = IO {
    println(s"$name: Sleeping...")
    try {
      Thread.sleep(1000)
      println(s"$name: Slept")
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }

  val sleepingWithBoundary = sleepingWith("first") >> IO.cancelBoundary >> sleepingWith("second")

  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      i <- sleepingWithBoundary.start
      _ <- i.cancel
      _ <- sleepingWith("orthogonal")
    } yield ()

    program.map(_ => ExitCode.Success)
  }

}
