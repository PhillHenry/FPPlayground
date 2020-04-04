package uk.co.odinconsultants.fp.cats.fs2.broadcast

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.Broadcast

object FanOutApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val processors = List[Function1[Int, Boolean]](
      v => v > 5,
      v => v > 10,
      // ...
    )

    val s = Stream(1,2,3,4)
      .through(Broadcast[IO, Int](minReady = 1))
      .take(processors.length)
      .zipWithIndex
      .flatMap { case (src, idx) => Stream(src.toString) }
//      .parJoinUnbounded


    IO(ExitCode.Error)
  }
}
