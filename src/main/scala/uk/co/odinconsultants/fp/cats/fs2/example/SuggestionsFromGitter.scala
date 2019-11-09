package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object SuggestionsFromGitter extends IOApp {

  import Settings._

  override def run(args: List[String]): IO[ExitCode] = {

    val stream = consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("test2"))
      .evalTap(consumer => IO(consumer.toString).void)
      .evalMap(IO.sleep(3.seconds).as) // sleep a bit to trigger potential race condition with _.stream
      .flatMap(_.stream)
      .map(committable => committable.record.key -> committable.record.value)
      .interruptAfter(10.seconds)

    val consumed =
      stream // wait some time to catch potentially duplicated records
        .compile
        .toVector
        .unsafeRunSync

    println(s"Consumed = $consumed")

    stream.compile.drain.as(ExitCode.Success)
  }
}
