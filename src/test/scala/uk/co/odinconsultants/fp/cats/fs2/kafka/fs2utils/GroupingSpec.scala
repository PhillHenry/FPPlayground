package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import org.scalatest.{Matchers, WordSpec}
import fs2.Stream

import scala.concurrent.duration._
import scala.concurrent.Await

class GroupingSpec extends WordSpec with Matchers {

  case class MyDatum(id: Int, value: String)

  val selector: MyDatum => Int = _.id


  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val F                              = implicitly[ConcurrentEffect[IO]]

  def datumFor(i: Int): MyDatum = MyDatum(i, i.toString)

  val noisyErrorHandler: Throwable => IO[Unit] = { t =>
    t.printStackTrace()
    IO.unit
  }

  "unbounded groupBy" should {
    val s = (2 to 10).foldLeft(Stream.emit(datumFor(1))) { case (acc, i) =>
      acc.interleave(
        Stream.emit(datumFor(i)).repeatN(i)
      )
    }

    "lead to streams for each key (?)" ignore {
      val pipe    = groupByUnbounded(selector)
      val result  = pipe(s)

      val io: IO[List[(Int, Stream[IO, MyDatum])]] = result.take(55).compile.toList
      val mapped = io.map { xs =>
        println(s"xs = $xs")
        xs should have length 10
      }
      Await.result(mapped.unsafeToFuture(), 1 second)
    }
  }


}
