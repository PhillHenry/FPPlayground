package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.Stream
import fs2.kafka._

import scala.concurrent.duration._

/**
 * https://ovotech.github.io/fs2-kafka/docs/consumers
 */
object ConsumerMain extends IOApp {

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

  override def run(args: List[String]): IO[ExitCode] = {
    // code taken (and bastardised) from https://ovotech.github.io/fs2-kafka/docs/consumers
    val cStream =
      consumerStream[IO]
        .using(consumerSettings)                      // Stream[F, KafkaConsumer[F, K, V]]
        .evalTap(subscribeFn)                         // Stream[F2, O]
        .flatMap(toStreamFn)                          // Stream[F2, O2]
        .mapAsync(25)(commitFn)         // Stream[F2, O2]
        .through(produce(producerSettings))           // Stream[F2, O2] (from the doc for through: "Transforms this stream using the given `Pipe`.")
        .map(passingThroughFn)                        // Stream[F, O2]
        .through(commitBatchWithin(500, 15.seconds))  // Stream[F2, O2]

    println("Draining stream")
    val result = cStream.compile.drain.as(ExitCode.Success)
    println("Done.")

    result
  }

}