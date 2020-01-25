package uk.co.odinconsultants.fp.cats

import cats.effect.{ExitCode, IO, IOApp, Timer}
import scala.concurrent.duration._
import cats.implicits._

object TraverseEtc extends IOApp {

  def work(x: Int): IO[Unit] = for {
    _ <- IO(println(s"Starting $x on ${Thread.currentThread().getName}"))
    _ <- Timer[IO].sleep(1.second)
    _ <- IO(println(s"Finished $x on ${Thread.currentThread().getName}"))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    val traverse_     = (0 to 10).toList.traverse_(work)
    val parTraverse   = (0 to 10).toList.parTraverse(work)
//    val parTraverseN  = (0 to 20).toList.parTraverseN
    IO { println("\ntraverse_") } *> traverse_ *>
      IO { println("\nparTraverse") } *> parTraverse *>
//      IO { println("\nparTraverseN")} *> parTraverseN *>
      IO(ExitCode.Success)

  }
}
