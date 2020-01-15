package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, IO, Timer}
import net.manub.embeddedkafka.EmbeddedKafka
import org.scalatest.WordSpec

import scala.concurrent.duration._

class EmbeddedKafkaSpec extends WordSpec with EmbeddedKafka {

  import ConsumerKafka._
  import ProducerKafka._

  "Consumers" should {
    "work with a real Kafka" in {

      implicit val testContext: TestContext       = TestContext()
      implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
      implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)

      withRunningKafka{

        cStream(printMessage).compile.toList.unsafeRunAsync(_.fold(x => println("failed"), x => ()))
        testContext.tick(1 seconds)
        pStream.compile.toList// .unsafeRunSync() <-- this just hangs
          .unsafeRunAsync(_.fold(x => println("failed"), x => ()))
        testContext.tick(1 seconds)
        Thread.sleep(5000)
      }(EmbeddedKafkaMain.embeddedKafkaConfig)
    }
  }

}
