package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.concurrent.Deferred
import cats.effect.{ExitCase, ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.duration._

object UncancellableMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val x: IO[Unit] = (IO(println("foo")) *> IO.never).start.uncancelable.flatMap(_.cancel)
    x.map(_ => ExitCode.Success) // always prints 'foo' for me
  }
}
