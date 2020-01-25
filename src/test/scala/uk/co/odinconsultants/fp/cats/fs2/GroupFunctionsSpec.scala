package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import fs2.Stream
import cats.implicits._

class GroupFunctionsSpec extends WordSpec with Matchers {

  import GroupFunctions._

  val kvs = Seq(1 -> "one", 2 -> "two", 1 -> "one again")

  s"Sequence ${kvs.mkString(", ")}" should {
    val ios = kvs.map{ case (k, v) =>  IO {
      println(s"$k = $v")
      k -> v
    }}
//    val s = Stream(ios).flatMap(_.map { io => Stream.eval(io) }.traverse )

    "be grouped by key" in {
//      groupingAdjacentBy(s)
    }
  }

}
