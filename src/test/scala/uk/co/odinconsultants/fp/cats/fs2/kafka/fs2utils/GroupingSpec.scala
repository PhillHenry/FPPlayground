package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import org.scalatest.{Matchers, WordSpec}
import fs2.Stream
import cats.implicits._

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.Await

class GroupingSpec extends WordSpec with Matchers {

  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val F                              = implicitly[ConcurrentEffect[IO]]

  import PHTestMain._

  "unbounded groupBy" should {
    "lead to streams for each key (?)" in {
      val pipe    = groupByUnbounded(selector)
      val result  = pipe(s)

      testContext.tick(1 second)
      val io: IO[List[(Int, Stream[IO, MyDatum])]] = result.compile.toList
      val mapped: IO[List[List[MyDatum]]] = io.flatMap { case xs =>
        println(s"xs = $xs")
        val ios: List[IO[List[MyDatum]]] = xs.map(_._2.compile.toList).toList
        val zs: IO[List[List[MyDatum]]] = ios.sequence
        zs
      }
      val xs = flatten(mapped)
      xs.foldLeft(0){ case (acc, x) => acc + x.size } shouldBe 55
      xs.foreach { ds =>
        ds.length shouldBe ds.head.id
      }
    }
  }


  private def flatten(mapped: IO[List[List[MyDatum]]]): List[List[MyDatum]] = {
    val xs: List[List[MyDatum]] = Await.result(mapped.unsafeToFuture(), 1 second)
    xs.foreach(x => println(s"${x.mkString(", ")}"))
    xs
  }
}
