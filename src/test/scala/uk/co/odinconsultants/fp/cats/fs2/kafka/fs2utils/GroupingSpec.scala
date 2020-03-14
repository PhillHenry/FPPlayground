package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import org.scalatest.{Matchers, WordSpec}
import fs2.Stream

class GroupingSpec extends WordSpec with Matchers {

  case class MyDatum(id: Int, value: String)

  val selector: MyDatum => Int = _.id


  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)
  implicit val F                              = implicitly[ConcurrentEffect[IO]]

  def datumFor(i: Int): IO[MyDatum] = IO ( MyDatum(i, i.toString) )

  "unbounded groupBy" should {
    val s = (2 to 10).foldLeft(Stream.emit(datumFor(1))) { case (acc, i) =>
      acc.interleave(
        Stream.emit(datumFor(i)).repeatN(i)
      )
    }

    "lead to streams for each key (?)" in {
      val pipe    = groupByUnbounded(selector)
//      val result  = pipe(s)
//      val xs      = result.take(55).toVector
//      xs should have length 10
    }
  }


}
