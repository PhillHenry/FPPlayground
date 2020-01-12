package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.kafka.{ProducerRecord, ProducerRecords, consumerStream, producerStream}
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{consumerSettings, producerSettings, topicName}
import ConsumerKafka._
import cats.implicits._

object SendAndReceiveMain extends IOApp {

  val pStream =
    producerStream[IO]
      .using(producerSettings)
      .flatMap { producer =>
        println(s"producer = $producer")
        val record  = ProducerRecord(topicName, "key", "value")
        Stream.eval(
          producer.produce(ProducerRecords.one(record, 1L))
        )
      }

  val cStream = kafkaConsumer
    .evalTap { kafkaConsumer =>
      println(s"evalTap: kafkaConsumer = $kafkaConsumer")
      kafkaConsumer.subscribeTo(topicName)
    }
    .flatMap { kafkaConsumer =>
      println(s"flatMap: kafkaConsumer = $kafkaConsumer")
      kafkaConsumer.partitionedStream
    }
    .map { partition => // "a Stream of records for a single topic-partition"
      println(s"partition = $partition")
      partition
        .map { committable =>
          println(s"committable = $committable")
        }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val consume: IO[Unit] = cStream.compile.drain
    val produce: IO[Unit] = pStream.compile.drain
    consume *> produce *> IO(ExitCode.Success)
  }
}
