package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.IO
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, ProducerSettings}

object Settings {
  val port = 9999

  val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(s"localhost:$port")
      .withGroupId("group")

  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers(s"localhost:$port")

}
