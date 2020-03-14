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

  case class MyDatum(id: Int, value: String)

  val selector: MyDatum => Int = _.id


  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val F                              = implicitly[ConcurrentEffect[IO]]

  def datumFor(i: Int): MyDatum = MyDatum(i, i.toString)

  type Compiled = List[(Int, Stream[IO, MyDatum])]
  def compile(s: Stream[IO, MyDatum]): IO[List[MyDatum]] = s.compile.toList

  "unbounded groupBy" should {
    val s = (2 to 10).foldLeft(Stream.emit(datumFor(1))) { case (acc, i) =>
      acc.interleave(
        Stream.emit(datumFor(i)).repeatN(i)
      )
    }

    "lead to streams for each key (?)" in {
      val pipe    = groupByUnbounded(selector)
      val result  = pipe(s)

      val io: IO[Compiled] = result.take(55).compile.toList
      val mapped = io.flatMap { case xs: Compiled =>
        println(s"xs = $xs")
        val ios: List[IO[List[MyDatum]]] = xs.map(_._2.compile.toList).toList
        val zs: IO[List[List[MyDatum]]] = ios.sequence
        zs
      }
      val xs: List[List[MyDatum]] = Await.result(mapped.unsafeToFuture(), 1 second)
      xs.foreach(x => println(s"${x.mkString(", ")}"))
    }
  }


}
