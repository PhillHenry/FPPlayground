package uk.co.odinconsultants.fp.cats.errors

import org.scalatest.{Matchers, WordSpec}

class MyOnErrorSpec extends WordSpec with Matchers {

  import MyOnError._

  "happy path" should {
    "yield 10" in {
      happyPath.unsafeRunSync() shouldBe 10
    }
  }

  "unhappy path" should {
    "throw exception upon unsafeRun" in {
      assertThrows[ArithmeticException] {
        explodingIO.unsafeRunSync()
      }
    }
    "be attempted" in {
      explodingIO.attempt.unsafeRunSync() match {
        case Left(x: ArithmeticException) => // expected
        case x => fail(s"Was note expecting $x")
      }
    }
  }

}
