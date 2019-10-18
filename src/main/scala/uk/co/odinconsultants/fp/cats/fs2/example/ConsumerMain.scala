package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object ConsumerMain extends IOApp {

  import Settings._

  override def run(args: List[String]): IO[ExitCode] = {
    def processRecord(record: ConsumerRecord[String, String]): IO[(String, String)] =
      IO.pure(record.key -> record.value)

    val cStream =
      consumerStream[IO]
        .using(consumerSettings)
        .evalTap(_.subscribeTo("test"))
        .flatMap(_.stream)
        .mapAsync(25) { committable =>
          println(s"committable = $committable")
          processRecord(committable.record)
            .map { case (key, value) =>
              val record = ProducerRecord("topic", key, value)
              println(s"record = $record")
              ProducerRecords.one(record, committable.offset)
            }
        }
        .through(produce(producerSettings))
        .map(_.passthrough)
        .through(commitBatchWithin(500, 15.seconds))

    println("Draining stream")
    val result = cStream.compile.drain.as(ExitCode.Success)
    println("Done.")

    result
  }
}