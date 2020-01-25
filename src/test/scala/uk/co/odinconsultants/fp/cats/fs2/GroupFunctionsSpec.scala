package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import fs2.{Chunk, Stream}
import cats.implicits._

class GroupFunctionsSpec extends WordSpec with Matchers {

  import GroupFunctions._

  type K  = Int
  type V  = String
  type KV = (K, V)

  val kvs: Seq[KV] = Seq(1 -> "one", 2 -> "two", 1 -> "one again")

  s"Sequence ${kvs.mkString(", ")}" should {
    val s: Stream[IO, KV] = Stream.fromIterator[IO](kvs.iterator)

    "be grouped by key" ignore {
      val sGrouped: Stream[IO, (K, Chunk[KV])] = groupingAdjacentBy(s)
      val grouped = sGrouped.compile.toList.unsafeRunSync()
      withClue(grouped.mkString(", ")) {
        grouped should have size kvs.map(_._1).toSet.size
      }
    }
  }

}
