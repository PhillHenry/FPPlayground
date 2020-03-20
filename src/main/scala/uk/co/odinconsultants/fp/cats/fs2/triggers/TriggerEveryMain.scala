package uk.co.odinconsultants.fp.cats.fs2.triggers

import cats.effect.{ExitCode, IO, IOApp}

import fs2.Stream
import scala.concurrent.duration._
import cats.implicits._

object TriggerEveryMain extends IOApp {
  val delayStream = Stream
    .iterate[IO, Int](
      1)(_ + 1)
    .metered(10.seconds)

  override def run(args: List[String]): IO[ExitCode] = {
    delayStream.switchMap(_ => Stream.awakeEvery[IO](1 second)).compile.toList *> IO(ExitCode.Success)
  }
}
