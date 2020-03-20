package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.effect.laws.util.TestContext
import cats.effect.{ConcurrentEffect, ContextShift, IO}
import cats.implicits._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class GroupingSpec extends WordSpec with Matchers {

  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val F                              = implicitly[ConcurrentEffect[IO]]

  import PHTestMain._

  "unbounded groupBy" should {
    "lead to streams for each key (?)" in {
      val pipe    = groupByUnbounded(selector)
      val result  = pipe(s)

      val ioList = result.compile.toList
      val mapped: IO[Flattened] = ioList.flatMap { case xs =>
        /*
        Michael Pilquist @mpilquist 15:04  The toList method is added as an extension method in the case where F[_] = Pure
        [Otherwise, you have to use compile]
         */
        val ios: List[IO[List[MyDatum]]] = xs.map(_._2.compile.toList)
        val zs: IO[Flattened] = ios.sequence
        zs
      }
      val xs = flatten(mapped)
      xs.foldLeft(0){ case (acc, x) => acc + x.size } shouldBe 55
      xs.foreach { ds =>
        ds.length shouldBe ds.head.id
      }
    }
  }

  type Flattened = List[List[MyDatum]]

  private def flatten(mapped: IO[Flattened]): List[List[MyDatum]] = {
    val xs: Flattened = Await.result(mapped.unsafeToFuture(), 1 second)
    xs.foreach(x => println(s"${x.mkString(", ")}"))
    xs
  }
}
