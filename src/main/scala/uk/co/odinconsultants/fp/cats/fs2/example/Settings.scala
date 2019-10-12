package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.IO
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, ProducerSettings}

object Settings {
  val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9999")
      .withGroupId("group")

  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:9999")

}
