package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.concurrent.Semaphore
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource, Timer}
import monix.eval.{Task, TaskApp}

import scala.concurrent.duration._
import cats.implicits._

object CommitSudoku2 extends IOApp {
  def safe[A, B](handleItem: A => IO[B]): Resource[IO, A => IO[B]] =
    Resource.make(Semaphore[IO](1))(_.acquire)
      .map { sem => (a: A) => sem.withPermit(handleItem(a)).uncancelable }

  def processItem(int: Int)(implicit timer: Timer[IO]): IO[Unit] =
    IO { println(s"Processing ${int}") } >>
      IO.sleep(20.seconds) >>
      IO { println(s"Done processing ${int}") }

  override def run(args: List[String]) = {
    safe(processItem).use { f =>
      def loop(a: Int): IO[Unit] = f(a) >> loop(a + 1)

      loop(0).as(ExitCode.Success)
    }
  }
}
