package uk.co.odinconsultants.fp.cats.fs2.kafka

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object TransactionalKafkaMain {

  val producerSettings =
    TransactionalProducerSettings(
      "transactional-id",
      ProducerSettings[IO, String, String]
        .withBootstrapServers("localhost")
    )

}
