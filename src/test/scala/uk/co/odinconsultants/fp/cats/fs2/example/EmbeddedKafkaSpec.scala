package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import fs2.kafka.{consumerStream, producerStream}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.WordSpec
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{consumerSettings, producerSettings, topicName}
import cats.implicits._

class EmbeddedKafkaSpec extends WordSpec with EmbeddedKafka {

  "Consumers" should {
    "work with a real Kafka" in {

      implicit val testContext: TestContext = TestContext()
      implicit val cs: ContextShift[IO] = testContext.contextShift(IO.ioEffect)
      implicit val timer: Timer[IO] = testContext.timer(IO.ioEffect)

      withRunningKafka{
        val cStream = consumerStream[IO]
          .using(consumerSettings)
          .evalTap { kafkaConsumer =>
            println(s"evalTap: kafkaConsumer = $kafkaConsumer")
            kafkaConsumer.subscribeTo(topicName)
          }
          .flatMap { kafkaConsumer =>
            println(s"flatMap: kafkaConsumer = $kafkaConsumer")
            kafkaConsumer.partitionedStream
          }
          .map { partition => // "a Stream of records for a single topic-partition"
            println(s"partition = $partition")
            partition
              .map { committable =>
                println(s"committable = $committable")
              }
          }
//        val pStream =
//          producerStream[IO]
//            .using(producerSettings)
//            .flatMap { producer =>
//              println(s"producer = $producer")
//              producer.
//            }
      }(EmbeddedKafkaMain.embeddedKafkaConfig)
    }
  }

}
