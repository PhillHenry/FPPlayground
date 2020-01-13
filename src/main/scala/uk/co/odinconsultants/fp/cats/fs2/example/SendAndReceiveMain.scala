package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerResult, consumerStream, producerStream}
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{consumerSettings, producerSettings, topicName}
import ConsumerKafka._
import cats.implicits._

object SendAndReceiveMain extends IOApp {

  val pStream: Stream[IO, IO[ProducerResult[String, String, Unit]]] =
    producerStream[IO]
      .using(producerSettings)
      .flatMap { producer =>
        val record    = ProducerRecord(topicName, "key", new java.util.Date().toString)
        val onRecord  = ProducerRecords.one(record)
        Stream.eval(producer.produce(onRecord))
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
    .flatMap { partition =>
      println(s"partition = $partition")
      partition
        .flatMap { committable =>
          Stream.eval(IO { println(s"committable = $committable") })
        }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val consume: IO[Unit] = cStream.compile.drain
    val produce: IO[Unit] = pStream.compile.drain
    (consume, produce).parMapN((_, _)=> ExitCode.Success)
  }
}
