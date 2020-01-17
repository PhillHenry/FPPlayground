package uk.co.odinconsultants.fp.cats.fs2

import org.scalatest.{Matchers, WordSpec}

class MyPullSpec extends WordSpec with Matchers {

  import MyPull._

  "Splicing" should {
    val n         = 6
    val expected  = (1 to n).toList

    def unsafeRunSyncToList(s: IntStream): List[Int] = s.compile.toList.unsafeRunSync()

    s"yield ${expected.mkString(",")} when using pulls" in {
      unsafeRunSyncToList(pullStreamOfNums(n)) shouldBe expected
    }

    s"yield ${expected.mkString(",")} for a pure streams" in {
      unsafeRunSyncToList(pureStreamOfNums(n)) shouldBe expected
    }

    s"yield ${expected.mkString(",")} for a stream" in {
      unsafeRunSyncToList(streamOfNums(n)) shouldBe expected
    }

    s"yield ${expected.mkString(",")} for IO stream" in {
      unsafeRunSyncToList(ioStreamOfNums(n)) shouldBe expected
    }
  }

}
