package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.Validated._
import cats.data.ValidatedNec
import cats.implicits._
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.ValidationFixture

class MyApplicativesSpec extends WordSpec with Matchers {

  "Validated" should {

    "return a Valid for the happy path" in new ValidationFixture {
      val x = new MyApplicatives(first.validNec, second.validNec, third.validNec)
      x.process shouldBe Valid(happyPath)
    }

  }

}
