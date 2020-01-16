package uk.co.odinconsultants.fp.cats.fs2

import org.scalatest.{Matchers, WordSpec}

class MyPullSpec extends WordSpec with Matchers {

  import MyPull._

  "Splicing" should {
    "not effect stream" in {
      streamOfNums(6).compile.toList.unsafeRunSync() shouldBe List(1,2,3,4,5,6)
    }
    "not effect effectful stream" in {
      effectfulStreamOfNums(6).compile.toList.unsafeRunSync() shouldBe List(1,2,3,4,5,6)
    }
  }

}
