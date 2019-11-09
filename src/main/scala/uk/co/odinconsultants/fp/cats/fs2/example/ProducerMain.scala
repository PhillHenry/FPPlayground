package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._

object ProducerMain  extends IOApp {

  import Settings._

  override def run(args: List[String]): IO[ExitCode] = {

    val pStream =
      producerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          println(s"producer = $producer")
          consumerStream[IO]
            .using(consumerSettings)
            .evalTap { kafkaConsumer =>
              println(s"kafkaConsumer = $kafkaConsumer")
              kafkaConsumer.subscribeTo(topicName)
            }
            .flatMap { kafkaConsumer =>
              println(s"kafkaConsumer = $kafkaConsumer")
              kafkaConsumer.partitionedStream
            }
            .map { partition =>
              println(s"partition = $partition")
              partition
                .map { committable =>
                  println(s"committable = $committable")
                  val key     = committable.record.key
                  val value   = committable.record.value
                  val record  = ProducerRecord(topicName, key, value)
                  ProducerRecords.one(record, committable.offset)
                }
                .through(produce(producerSettings, producer))
            }
            .parJoinUnbounded
        }

    pStream.compile.drain.as(ExitCode.Success)
  }
}
