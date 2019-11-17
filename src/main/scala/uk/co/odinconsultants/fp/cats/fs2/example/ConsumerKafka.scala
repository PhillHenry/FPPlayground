package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.{Pipe, Stream}
import fs2.kafka.{CommittableConsumerRecord, CommittableOffset, ConsumerRecord, KafkaConsumer, ProducerRecord, ProducerRecords, ProducerResult, consumerStream}

import scala.concurrent.duration._

object ConsumerKafka {

  type MyPipe = Stream[IO, ProducerRecords[String, String, CommittableOffset[IO]]] => Stream[IO, ProducerResult[String, String, CommittableOffset[IO]]]

  type BatchPipe = Pipe[IO, CommittableOffset[IO], Unit]

  import Settings._

  type MyKafkaConsumer = KafkaConsumer[IO, String, String]

  def processRecord(record: ConsumerRecord[String, String]): IO[(String, String)]
  = IO.pure(record.key -> record.value)

  val subscribeFn: MyKafkaConsumer => IO[Unit]
  = _.subscribeTo(topicName)

  /**
   * When using stream, records on all assigned partitions end up in the same Stream.
   */
  val toStreamFn: MyKafkaConsumer => Stream[IO, CommittableConsumerRecord[IO, String, String]]
  = _.stream

  val commitFn: CommittableConsumerRecord[IO, String, String] => IO[ProducerRecords[String, String, CommittableOffset[IO]]] = { committable =>
    println(s"committable = $committable")
    val io: IO[(String, String)] = processRecord(committable.record)
    io.map { case (key, value) =>
      val record = ProducerRecord("topic", key, value)
      println(s"record = $record")
      ProducerRecords.one(record, committable.offset)
    }
  }

  val passingThroughFn: ProducerResult[String, String, CommittableOffset[IO]] => CommittableOffset[IO] = _.passthrough

  def kafkaConsumer(implicit io: ConcurrentEffect[IO], context: ContextShift[IO], timer: Timer[IO]): Stream[IO, KafkaConsumer[IO, String, String]] =
    consumerStream[IO]
      .using(consumerSettings)

  def producerPipe(implicit f: ConcurrentEffect[IO],  context: ContextShift[IO]): MyPipe =
    fs2.kafka.produce(producerSettings)(f, context)

  def committingBatch(implicit io: ConcurrentEffect[IO], timer: Timer[IO]): BatchPipe = fs2.kafka.commitBatchWithin(500, 15.seconds)

}
