package uk.co.odinconsultants.fp.cats.fs2.example

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}

object EmbeddedKafkaMain extends EmbeddedKafka {

  def main(args: Array[String]): Unit = {
    val customKafkaConfig = embeddedKafkaConfig

    withRunningKafka(Thread.sleep(1000000))(customKafkaConfig)
  }


  def embeddedKafkaConfig:EmbeddedKafkaConfig  = {
    val customBrokerConfig = Map("replica.fetch.max.bytes" -> "2000000",
      "message.max.bytes" -> "2000000")

    val customProducerConfig = Map("max.request.size" -> "2000000")
    val customConsumerConfig = Map("max.partition.fetch.bytes" -> "2000000")
    EmbeddedKafkaConfig(
      kafkaPort = Settings.port,
      customBrokerProperties = customBrokerConfig,
      customProducerProperties = customProducerConfig,
      customConsumerProperties = customConsumerConfig)
  }
}
