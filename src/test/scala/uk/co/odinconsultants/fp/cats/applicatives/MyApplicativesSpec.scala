package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.ValidatedNec
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.ValidationFixture
import cats.data._
import cats.data.Validated._
import cats.implicits._

class MyApplicativesSpec extends WordSpec with Matchers {

  "Validated" should {

    type MyValidated = ValidatedNec[Exception, String]

    "return a Valid for the happy path" in new ValidationFixture {
      val x = new MyApplicatives(first.validNec, second.validNec, third.validNec)
      import x._
      x.process shouldBe Valid(MyStruct(first, second, third))
    }
  }

}
