package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._

/**
 * https://ovotech.github.io/fs2-kafka/docs/consumers
 */
object ConsumerMain extends IOApp {

  import ConsumerKafka._

  override def run(args: List[String]): IO[ExitCode] = {
    // code taken (and bastardised) from https://ovotech.github.io/fs2-kafka/docs/consumers
    val cStream =
      kafkaConsumer
        .evalTap(subscribeFn)                         // Stream[F2, O]
        .flatMap(toStreamFn)                          // Stream[F2, O2]
        .mapAsync(25)(commitFn)         // Stream[F2, O2]
        .through(producerPipe)                        // Stream[F2, O2] (from the doc for through: "Transforms this stream using the given `Pipe`.")
        .map(passingThroughFn)                        // Stream[F, O2]
        .through(committingBatch)                     // Stream[F2, O2]

    println("Draining stream")
    val result = cStream.compile.drain.as(ExitCode.Success)
    println("Done.")

    result
  }

}