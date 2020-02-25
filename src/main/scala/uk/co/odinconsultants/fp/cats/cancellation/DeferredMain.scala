package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.concurrent.Deferred
import cats.effect.{ExitCase, ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.duration._

object DeferredMain extends IOApp {

  /**

Jakub Kozłowski @kubukoz Feb 20 21:16
this times out... after one second
allocationStarted has been completed, so (allocationStarted.complete(()) *> IO.never) has started executing, but the guaranteeCase branch doesn't run
having a single start makes it work. But I don't see why it wouldn't work with two starts

Piotr Gawryś @Avasil Feb 20 21:24
We don't propagate cancelation to "children" so I would say it is expected
   */
  override def run(args: List[String]): IO[ExitCode] = {
    val x: IO[ExitCase[Throwable]] = Deferred[IO, Unit].flatMap { allocationStarted =>
      Deferred[IO, ExitCase[Throwable]].flatMap { allocationExitCase =>
        val performFetch =
          (allocationStarted.complete(()) *> IO.never).guaranteeCase(allocationExitCase.complete)

        performFetch.start.start.flatMap(allocationStarted.get *> _.cancel) *>
          allocationExitCase.get.timeout(1.second)
      }
    }

    x.map(_ => ExitCode.Success)
  }
}
