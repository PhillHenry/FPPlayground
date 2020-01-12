package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import fs2.kafka.{ProducerRecord, ProducerRecords, consumerStream, producerStream}
import fs2.Stream
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.WordSpec
import uk.co.odinconsultants.fp.cats.fs2.example.Settings.{consumerSettings, producerSettings, topicName}
import cats.implicits._
import scala.concurrent.duration.{Duration, FiniteDuration, TimeUnit}
import scala.concurrent.duration._

class EmbeddedKafkaSpec extends WordSpec with EmbeddedKafka {


  import SendAndReceiveMain._

  "Consumers" should {
    "work with a real Kafka" in {

      implicit val testContext: TestContext = TestContext()
      implicit val cs: ContextShift[IO] = testContext.contextShift(IO.ioEffect)
      implicit val timer: Timer[IO] = testContext.timer(IO.ioEffect)

      withRunningKafka{

        cStream.compile.toList.unsafeRunAsync(_.fold(x => println("failed"), x => ()))
        testContext.tick(1 seconds)
        pStream.compile.toList// .unsafeRunSync() <-- this just hangs
          .unsafeRunAsync(_.fold(x => println("failed"), x => ()))
        testContext.tick(1 seconds)
        Thread.sleep(5000)
      }(EmbeddedKafkaMain.embeddedKafkaConfig)
    }
  }

}
