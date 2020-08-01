package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.{ExitCode, IO, IOApp}
import uk.co.odinconsultants.fp.cats.cancellation.CancelBoundaryMain.sleeping
import cats.implicits._

object StartCancelMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val result = for {
      f <- (sleeping >> sleeping >> sleeping).start
      _ <- f.cancel
      _ <- f.join
    } yield ()

    result.map(_ => ExitCode.Success)
  }

}
