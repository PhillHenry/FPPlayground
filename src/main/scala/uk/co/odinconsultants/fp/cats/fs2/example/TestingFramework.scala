package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

object TestingFramework extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = Stream.emits(List(1,2,3)).evalMap { x => IO { println(s"x = $x")} }

    stream.compile.drain.map { _ => ExitCode.Success }
  }

}
