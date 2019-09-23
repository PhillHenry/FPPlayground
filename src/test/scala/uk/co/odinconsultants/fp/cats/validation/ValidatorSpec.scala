package uk.co.odinconsultants.fp.cats.validation

import cats.implicits._
import org.scalatest.{Matchers, WordSpec}

class ValidatorSpec extends WordSpec with Matchers {

  val first     = "Hello"
  val second    = "World"
  val third     = "(the end)"
  val expected  = s"$first $second $third"

  "Options" should {

    "return something for the happy path" in {
      val TW = Option(first)
      val UW = Option(second)
      val VW = Option(third)
      val x = new Validator(TW, UW, VW)
      x.process shouldBe Some(expected)
    }
  }

  "Eithers" should {

    type MyEither = Either[Exception, String]

    "return something for the happy path" in {
      val TW: MyEither = Right(first)
      val UW: MyEither = Right(second)
      val VW: MyEither = Right(third)
      val x = new Validator(TW, UW, VW)
      x.process shouldBe Right(expected)
    }
  }

}
