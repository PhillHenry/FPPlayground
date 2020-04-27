package uk.co.odinconsultants.fp.cats.applicatives

import cats.effect.{ExitCode, IO, IOApp}
import uk.co.odinconsultants.fp.cats.applicatives.MyParThreads.doPrintLine

object MyParThreadsIOApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    import cats.implicits._
    (doPrintLine("Line 1"), doPrintLine("Line 2"), doPrintLine("Line 3")).parTupled.as(ExitCode.Success)
  }
}
