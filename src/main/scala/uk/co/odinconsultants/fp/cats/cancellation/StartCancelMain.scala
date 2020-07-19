package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.{ExitCode, IO, IOApp}
import uk.co.odinconsultants.fp.cats.cancellation.CancelBoundaryMain.sleeping
import cats.implicits._

object StartCancelMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val result = for {
      f <- (sleeping >> sleeping).start
      _ <- f.cancel
      _ <- IO { Thread.sleep(1500) }
    } yield ()

    result.map(_ => ExitCode.Success)
  }

}
