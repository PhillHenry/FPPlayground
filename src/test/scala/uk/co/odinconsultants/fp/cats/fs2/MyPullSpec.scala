package uk.co.odinconsultants.fp.cats.fs2

import org.scalatest.{Matchers, WordSpec}

class MyPullSpec extends WordSpec with Matchers {

  import MyPull._

  "Splicing" should {
    val n         = 6
    val expected  = (1 to n).toList

    def unsafeRunSyncToList(s: IntStream): List[Int] = s.compile.toList.unsafeRunSync()

    "not effect pure streams" in {
      unsafeRunSyncToList(pureStreamOfNums(n)) shouldBe expected
    }

    "not effect effectful stream" in {
      unsafeRunSyncToList(streamOfNums(n)) shouldBe expected
    }

    "not effect IO stream" in {
      unsafeRunSyncToList(ioStreamOfNums(n)) shouldBe expected
    }
  }

}
