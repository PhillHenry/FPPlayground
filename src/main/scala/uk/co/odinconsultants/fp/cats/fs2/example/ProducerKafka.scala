package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.implicits._
import fs2.Stream
import fs2.kafka._
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{producerSettings, topicName}

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

}
