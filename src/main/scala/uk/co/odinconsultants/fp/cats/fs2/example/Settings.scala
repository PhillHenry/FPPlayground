package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.IO
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, ProducerSettings}

object Settings {

  val topicName = "test2"

  val port = 9092

  val byteDeserializer = Deserializer.lift(bytes => IO.pure(if (bytes == null) "" else new String(bytes.dropWhile(_ == 0))))

  val consumerSettings: ConsumerSettings[IO, String, String] =
    ConsumerSettings[IO, String, String](
      keyDeserializer = byteDeserializer,
      valueDeserializer = byteDeserializer
    )
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(s"localhost:$port")
      .withGroupId("group")

  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers(s"localhost:$port")

}
