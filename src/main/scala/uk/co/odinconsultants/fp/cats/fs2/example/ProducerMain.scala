package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object ProducerMain  extends IOApp {

  import Settings._

  override def run(args: List[String]): IO[ExitCode] = {

    val pStream =
      producerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          consumerStream[IO]
            .using(consumerSettings)
            .evalTap(_.subscribeTo("topic"))
            .flatMap(_.partitionedStream)
            .map { partition =>
              partition
                .map { committable =>
                  val key = committable.record.key
                  val value = committable.record.value
                  val record = ProducerRecord("topic", key, value)
                  ProducerRecords.one(record, committable.offset)
                }
                .through(produce(producerSettings, producer))
            }
            .parJoinUnbounded
        }

    pStream.compile.drain.as(ExitCode.Success)


  }
}
