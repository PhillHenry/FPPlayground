package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ContextShift, IO, Timer}
import cats.effect.laws.util.TestContext
import org.scalatest.{Matchers, WordSpec}
import fs2.{Chunk, Stream}
import cats.implicits._

import scala.concurrent.Await
import scala.concurrent.duration._

class GroupFunctionsSpec extends WordSpec with Matchers {


  implicit val testContext: TestContext       = TestContext()
  implicit val cs:          ContextShift[IO]  = testContext.contextShift(IO.ioEffect)
  implicit val timer:       Timer[IO]         = testContext.timer(IO.ioEffect)


  import GroupFunctions._

  type K  = Int
  type V  = String
  type KV = (K, V)

  val kvs: Seq[KV] = Seq(1 -> "one", 2 -> "two", 1 -> "one again")

  s"Sequence ${kvs.mkString(", ")}" should {
    val s: Stream[IO, KV] = Stream.fromIterator[IO](kvs.iterator).evalMap { kv => IO {
      println(s"kv = $kv")
      kv
    }}

    "be grouped by key within a chunk" in {
      val sGrouped: Stream[IO, UniqueGroups[K, V]] = s.groupWithin(kvs.size, 1.second).map(c => grouping(c, identity[K]))
      val f = sGrouped.compile.toList.unsafeToFuture()
      testContext.tick(1.second)
      val chunks = Await.result(f, 1.second)
      chunks should have size 1
      chunks(0) should have size kvs.map(_._1).toSet.size
    }
  }

}
