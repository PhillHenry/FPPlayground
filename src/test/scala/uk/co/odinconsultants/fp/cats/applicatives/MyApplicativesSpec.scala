package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.Validated._
import cats.data.{Chain, ValidatedNec}
import cats.implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.{ValidationFixture, ValidatorFixture}
@RunWith(classOf[JUnitRunner])
class MyApplicativesSpec extends WordSpec with Matchers {

  "Validated" should {

    type MyValidated = ValidatedNec[Exception, String]

    "return a Valid for the happy path" in new ValidationFixture {
      val x = new MyApplicatives(first.validNec, second.validNec, third.validNec)
      x.process shouldBe Valid(happyPath)
    }

    "return a list of errors for the unhappy path" in new ValidationFixture {
      private val invalid1: ValidatedNec[String, String]  = first.invalidNec
      private val invalid2: ValidatedNec[String, String]  = third.invalidNec
      private val valid:    ValidatedNec[Nothing, String] = second.validNec

      val x = new MyApplicatives(invalid1, valid, invalid2)
      x.process shouldBe Invalid(Chain(first, third))
    }
  }

  "Options" should {
    "be just as good " in new ValidatorFixture {
      val x = new MyApplicatives(o1, o2, o3)
      x.process shouldBe Some(happyPath)
    }
  }

  "Either" should {
    "be just as good " in new ValidatorFixture {
      val x = new MyApplicatives(r1, r2, r3)
      x.process shouldBe Right(happyPath)
    }
  }

}
