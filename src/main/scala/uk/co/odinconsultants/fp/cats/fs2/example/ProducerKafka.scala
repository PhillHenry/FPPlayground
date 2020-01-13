package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerResult, producerStream}
import uk.co.odinconsultants.fp.cats.fs2.example.ConsumerKafka._
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{producerSettings, topicName}
import cats.implicits._

object ProducerKafka {

  type PResult = ProducerResult[String, String, Unit]

  def pStream(implicit io: ConcurrentEffect[IO], context: ContextShift[IO], timer: Timer[IO]): Stream[IO, PResult] =
    producerStream[IO]
      .using(producerSettings)
      .flatMap { producer =>
        recordStream(producer)
      }

  def recordStream(producer: KafkaProducer[IO, String, String]): Stream[IO, PResult] = {
    val record = ProducerRecord(topicName, "key", new java.util.Date().toString)
    val aRecord = ProducerRecords.one(record)
    val result: IO[PResult] = producer.produce(aRecord).flatten
    Stream.eval(result)
  }

  val printMessage: MyCommittableConsumerRecord => IO[Unit] = committable => IO { println(s"committable = ${committable}") }

  def cStream(io: MyCommittableConsumerRecord => IO[Unit])(implicit ce: ConcurrentEffect[IO], context: ContextShift[IO], timer: Timer[IO]): Stream[IO, Unit] = kafkaConsumer
    .evalTap (subscribeFn)
    .flatMap (partitionStreamsFn)
    .flatMap { partitionStream =>
      println(s"partition = $partitionStream")
      partitionStream
        .flatMap { committable =>
          Stream.eval(io(committable))
        }
    }

}
