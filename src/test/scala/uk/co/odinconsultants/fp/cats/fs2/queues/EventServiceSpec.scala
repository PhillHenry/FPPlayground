package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.{SignallingRef, Topic}
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

class EventServiceSpec extends WordSpec with Matchers {

  "queue and dequeue" should {

    implicit val testContext: TestContext       = TestContext()
    implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
    implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)

    def wait(msg: String): Unit = {
      val duration: FiniteDuration = 30 seconds
      val time = duration.toString
      println(s"${new java.util.Date()}: Waiting $time for $msg")
      testContext.tick(duration)
    }

    "result in an empty queue" in {

      Stream.eval {
        Topic[IO, Event](Text("Initial Event")) product SignallingRef[IO, Boolean](false)
      }.flatMap { case (topic, signal) =>
        val service = new EventService[IO](topic, signal)
        val concurrentStream = service.startPublisher.concurrently(service.startSubscribers)

        val checkSubscribeSize: Stream[IO, Unit] = topic.subscribeSize(3).flatMap { case (event, count) =>
          println(s"checkSubscribeSize: count = $count, event = $event")
          val check: IO[Unit] = IO {
            println(s"event => $event, count = $count")
            count shouldBe 1
          }
          Stream.eval(check)
        }

        concurrentStream.take(10) ++ checkSubscribeSize
      }.compile.drain.unsafeRunAsync(_.fold(x => println(s"fail $x"), x => s"success $x"))
//      }.compile.drain.unsafeRunSync()
      wait("after the whole damn thing")
      wait("and again")
    }
  }

}
