package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._

object ResourceProductMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val a = Resource.make(IO(println("acquire A")))(_ => IO(println("release A")))
    val b = Resource.make(IO(println("acquire B")))(_ => IO(println("release B")))

    val io: IO[Unit] = a.product(b).use(_ => IO(println("use")))

    io.as(ExitCode.Success)
  }

}
