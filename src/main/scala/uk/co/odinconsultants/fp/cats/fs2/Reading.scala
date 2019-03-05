package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

import scala.concurrent.duration._

object Reading  extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val s               = Stream.range[IO](1, 10).metered(25.millis)
    val headCompileLast = s.head.compile.lastOrError
    println(headCompileLast.unsafeRunSync()) // "1", as you'd expect
    IO(ExitCode.Success)
  }

}
