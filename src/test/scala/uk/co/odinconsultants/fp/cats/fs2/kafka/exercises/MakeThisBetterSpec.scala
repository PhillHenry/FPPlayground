package uk.co.odinconsultants.fp.cats.fs2.kafka.exercises

import org.scalatest.{Matchers, WordSpec}
import net.manub.embeddedkafka.EmbeddedKafka
import uk.co.odinconsultants.fp.cats.fs2.example.EmbeddedKafkaMain

import scala.concurrent.duration._
import scala.concurrent.Await

class MakeThisBetterSpec extends WordSpec with Matchers with EmbeddedKafka {

  import MakeThisBetter._

  "Message counts" should {
    "be correct" in {

      withRunningKafka {
        val ioCounts = messageCount("testTopic")
        val counts = Await.result(ioCounts.unsafeToFuture(), 11 seconds)
        counts should be (empty)
      }(EmbeddedKafkaMain.embeddedKafkaConfig)
    }
  }

}
