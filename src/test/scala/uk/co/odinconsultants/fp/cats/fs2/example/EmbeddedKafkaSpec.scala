package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, ExitCode, IO, Timer}
import net.manub.embeddedkafka.EmbeddedKafka
import org.scalatest.WordSpec

import scala.concurrent.Await
import scala.concurrent.duration._
import cats.implicits._
import fs2.Stream

class EmbeddedKafkaSpec extends WordSpec with EmbeddedKafka {

  import ConsumerKafka._
  import ProducerKafka._

  "Consumers" should {
    "work with a real Kafka" ignore {

      implicit val testContext: TestContext       = TestContext()
      implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
      implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)

      withRunningKafka{

        val io: IO[ExitCode] = SendAndReceiveMain.run(List.empty)

        testContext.tick(1 seconds)
        val f = Stream.eval(io).compile.drain.unsafeToFuture()
//          .unsafeRunAsync(_.fold(x => println("failed"), x => println(s"producer = $x")))
        testContext.tick(1 seconds)

        println("Sleeping...")
        val results = Await.result(f, 200.seconds)
        println(s"results = $results")
      }(EmbeddedKafkaMain.embeddedKafkaConfig)
    }
  }

}
