package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

object CornerCasesMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val print = IO { println("hello") }
    val s     = Stream.eval(print) ++ Stream.eval(IO.raiseError(new Throwable("test"))) ++ Stream.eval(print)
    s.attempt.compile.drain.map(_ => ExitCode.Success)
  }

}
