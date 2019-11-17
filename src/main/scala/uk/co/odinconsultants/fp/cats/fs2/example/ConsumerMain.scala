package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.Stream
import fs2.kafka.{CommittableOffset, ProducerRecords, ProducerResult}

import scala.concurrent.duration._

/**
 * https://ovotech.github.io/fs2-kafka/docs/consumers
 */
object ConsumerMain extends IOApp {

  import ConsumerKafka._
  import Settings._

//  type MyPipe = Stream[IO, ProducerRecords[String, String, CommittableOffset[IO]]] => Stream[IO, ProducerResult[String, String, Nothing]]
//  type MyPipe = Stream[IO, ProducerRecords[String, String, Nothing]] => Stream[IO, ProducerResult[String, String, Nothing]]
  type MyPipe[T] = Stream[IO, ProducerRecords[String, String, T]] => Stream[IO, ProducerResult[String, String, Nothing]]

  override def run(args: List[String]): IO[ExitCode] = {
    // code taken (and bastardised) from https://ovotech.github.io/fs2-kafka/docs/consumers
    val cStream =
      kafkaConsumer
        .evalTap(subscribeFn)                         // Stream[F2, O]
        .flatMap(toStreamFn)                          // Stream[F2, O2]
        .mapAsync(25)(commitFn)         // Stream[F2, O2]
        .through(fs2.kafka.produce(producerSettings)) // Stream[F2, O2] (from the doc for through: "Transforms this stream using the given `Pipe`.")
        .map(passingThroughFn)                        // Stream[F, O2]
        .through(fs2.kafka.commitBatchWithin(500, 15.seconds))  // Stream[F2, O2]

    println("Draining stream")
    val result = cStream.compile.drain.as(ExitCode.Success)
    println("Done.")

    result
  }


}