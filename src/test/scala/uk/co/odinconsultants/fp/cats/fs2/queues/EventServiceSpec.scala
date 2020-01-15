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

    val expectSuccess: Either[Throwable, Unit] => Unit = { cb =>
      cb.fold({ x => println(s"fail $x")
        fail(x)
      }, x =>"success")
    }

    def runTest(assertions: Topic[IO, Event] => Stream[IO, Unit], assertAfter: Int): Unit = Stream.eval {
        Topic[IO, Event](Text("Initial Event")) product SignallingRef[IO, Boolean](false)
      }.flatMap { case (topic, signal) =>
        val service           = new EventService[IO](topic, signal)
        val concurrentStream  = service.startPublisher.concurrently(service.startSubscribers)
        concurrentStream.take(assertAfter) ++ assertions(topic) ++ concurrentStream.drop(assertAfter)
      }.compile.drain.unsafeRunAsync(expectSuccess)


    "result in an empty queue" in {
      def checkSubscribeSize(topic: Topic[IO, Event]): Stream[IO, Unit] = topic.subscribers.flatMap { count =>
        println(s"checkSubscribeSize: count = $count")
        val check: IO[Unit] = if (count == 2) IO {
          println(s"count = $count")
        } else IO.raiseError(new Throwable(s"count = $count, expected 1"))
        Stream.eval(check)
      }

      runTest(checkSubscribeSize _, 1)

      wait("after the whole damn thing")
    }
  }

}
