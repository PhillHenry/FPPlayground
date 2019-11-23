package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._

/**
 * https://ovotech.github.io/fs2-kafka/docs/consumers
 */
object ConsumerMain extends IOApp {

  import ConsumerKafka._

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = pipeline(kafkaConsumer, subscribeFn, toStreamFn, commitFn, producerPipe, passingThroughFn, committingBatch)

    println("Draining stream")
    val result = stream.compile.drain.as(ExitCode.Success)
    println("Done.")

    result
  }

  import fs2.{Stream, Pipe}
  /**
   * @tparam K KafkaConsumer
   * @tparam C CommittableConsumerRecord
   * @tparam P MyProducer (ProducerRecords)
   * @tparam R MyProducerResult (ProducerResult)
   * @tparam O CommittableOffset[IO]
   * @tparam T final type
   */
  def pipeline[K, C, P, R, O, T](s:              Stream[IO, K],
                                 subscribe:      K => IO[Unit],
                                 toRecords:      K => Stream[IO, C],
                                 commitRead:     C => IO[P],
                                 producerPipe:   Pipe[IO, P, R],
                                 toWriteRecords: R => O,
                                 commitWrite:    Pipe[IO, O, T]): Stream[IO, T] = {
    s.evalTap(subscribe).flatMap(toRecords).mapAsync(25)(commitRead).through(producerPipe).map(toWriteRecords).through(commitWrite)
  }

}