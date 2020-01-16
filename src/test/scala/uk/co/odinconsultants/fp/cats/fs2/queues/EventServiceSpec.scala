package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.concurrent.{SignallingRef, Topic}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class EventServiceSpec extends WordSpec with Matchers {

  "queue and dequeue" should {
    implicit val testContext: TestContext       = TestContext()
    implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
    implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)

    def runTest(assertOn: Topic[IO, Event] => Stream[IO, Unit], pivot: Int): Future[Unit] = Stream.eval {
        Topic[IO, Event](Text("Initial Event")) product SignallingRef[IO, Boolean](false)
      }.flatMap { case (topic, signal) =>
        val service = new EventService[IO](topic, signal)
        val s: Stream[IO, Unit]       = service.startPublisher.concurrently(service.startSubscribers)
        s.take(pivot) ++ assertOn(topic) ++ s.drop(pivot)
      }.compile.drain.unsafeToFuture()

    def checkSubscribeSize(expected: Int)(topic: Topic[IO, Event]): Stream[IO, Unit] = topic.subscribers.flatMap { count =>
      val check: IO[Unit] = if (count == expected) IO {
        println(s"got expected count, $count")
      } else {
        IO.raiseError(new Throwable(s"count = $count, expected $expected"))
      }
      Stream.eval(check)
    }.take(1)

    "have 1 subscriber near the beginning" in {
      val f = runTest(checkSubscribeSize(1), 1)

      testContext.tick(60 seconds)
      Await.result(f, 2 seconds)
      f.isCompleted shouldBe true
      assert(testContext.state.lastReportedFailure == None)
    }
  }

}
