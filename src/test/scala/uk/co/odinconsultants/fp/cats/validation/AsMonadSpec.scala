package uk.co.odinconsultants.fp.cats.validation

import cats.data.NonEmptyList
import org.scalatest.{Matchers, WordSpec}

class AsMonadSpec extends WordSpec with Matchers {

  "Either" should {
    "be applicable" in new EitherFixture {
      import cats.implicits._
      val x = new AsMonad[MyValidated, Throwable]
      x.allOrNothing(mixedList) shouldBe invalid1
    }
    "be flatmappable" in new EitherFixture {
      import cats.implicits._
      val x = new AsMonad[MyValidated, Throwable]
      val allGood = new NonEmptyList(valid1, List(valid2))
      val result = for {
        y <- x.allOrNothing(allGood)
        z <- x.allOrNothing(allGood)
      } yield z
      println(result)
      result shouldBe valid2
    }
  }

}
