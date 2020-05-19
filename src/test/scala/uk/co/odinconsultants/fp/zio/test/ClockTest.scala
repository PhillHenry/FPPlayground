package uk.co.odinconsultants.fp.zio.test

import zio.duration._
import zio.{RIO, Schedule, Queue, UIO, URIO}
import zio.console._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestClock

object ClockTest extends DefaultRunnableSpec {

  val queue: UIO[Queue[String]] = Queue.bounded[String](10)

  def sendAsync(msg: String)(q: Queue[String]): UIO[Unit] =
    for {
      _ <- q.offer(msg).fork
    } yield ()

  def listen(q: Queue[String]): RIO[Console, List[String]] = {
    import scala.collection.mutable.ListBuffer
    val collector = new ListBuffer[String]()
    for {
      s <- q.poll
      _ <- putStrLn(s"Received $s")
      if (s.isDefined)
    } yield {
      if (collector.size == 5) collector.clear()
      (collector += s.get).toList
    }
  }

  val every5Min = Schedule.spaced(1.second)
  val until5Collected = Schedule.doUntil[List[String]](_.size == 5)
  val every5SecUntilCollected = until5Collected && every5Min

  val val2Test = {
    for {
      q <- queue
      _ <- URIO.foreach(1 to 20)(i => sendAsync(s"Number $i")(q))
      l <- listen(q).repeat(every5SecUntilCollected)
    } yield l
  }

  def spec = suite("Testing Spec")(
    test("test simple values") {
      assert(5*5)(equalTo(25))
    },
    testM("test Monadic/ZIO values") {
      /**
      Received Some(Number 7)
Warning: A test is using time, but is not advancing the test clock, which may result in the test hanging. Use TestClock.adjust to manually advance the time.
In Suite "Testing Spec", test "test Monadic/ZIO values" has taken more than 1 m to execute. If this is not expected, consider using TestAspect.timeout to timeout runaway tests for faster diagnostics.
       with:

        tup <- val2Test
        _   <- TestClock.adjust(5.seconds)

       but!

       toxicafunk05/17/2020
ah, it works if I move the adjust BEFORE calling val2Test :slight_smile:
       */
      for {
        _   <- TestClock.adjust(5.seconds)
        tup <- val2Test
      } yield assert(tup._1.size)(equalTo(5)) && assert(tup._2)(equalTo(4))
    }
  )
}
