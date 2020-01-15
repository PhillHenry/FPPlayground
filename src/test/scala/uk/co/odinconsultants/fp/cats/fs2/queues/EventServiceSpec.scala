package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.{SignallingRef, Topic}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class EventServiceSpec extends WordSpec with Matchers {

  "queue and dequeue" should {

    implicit val testContext: TestContext       = TestContext()
    implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
    implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)

    def runTest(assertions: Topic[IO, Event] => Stream[IO, Unit], assertAfter: Int): Future[Unit] = Stream.eval {
        Topic[IO, Event](Text("Initial Event")) product SignallingRef[IO, Boolean](false)
      }.flatMap { case (topic, signal) =>
        val service           = new EventService[IO](topic, signal)
        val concurrentStream  = service.startPublisher.concurrently(service.startSubscribers)
        concurrentStream.take(assertAfter) ++ assertions(topic) ++ concurrentStream.drop(assertAfter)
      }.compile.drain.unsafeToFuture()


    def checkSubscribeSize(expected: Int)(topic: Topic[IO, Event]): Stream[IO, Unit] = topic.subscribers.flatMap { count =>
      println(s"checkSubscribeSize: count = $count")
      val check: IO[Unit] = if (count == expected) IO {
        println(s"count = $count")
      } else {
        println("raising error")
        IO.raiseError(new Throwable(s"count = $count, expected $expected"))
      }
      Stream.eval(check)
    }

    "have 1 subscriber near the beginning" ignore {

      val f = runTest(checkSubscribeSize(1), 1)

      testContext.tick(2 seconds)
      Await.result(f, 2 seconds)
      f.isCompleted shouldBe true
      assert(testContext.state.lastReportedFailure == None)
    }
  }

}
