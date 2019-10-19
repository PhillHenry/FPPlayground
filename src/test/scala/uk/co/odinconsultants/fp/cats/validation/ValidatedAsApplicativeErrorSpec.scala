package uk.co.odinconsultants.fp.cats.validation

import cats.ApplicativeError
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.applicatives.MyApplicativeErrors

class ValidatedAsApplicativeErrorSpec extends WordSpec with Matchers {

  "Validated" should {
    "have an ApplicatativeError if its type has a semigroup" in new ValidatedFixture {
      import cats.data.Validated._
      import cats.implicits._

      val x = new ValidatedAsApplicative[MyValidated, MyErrorType]

      val allValidated = x.allOrNothing(mixedList)
      println(s"allOrNothing = $allValidated")
      allValidated shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

  "Happy path execution" should {
    "produce a F representing success" in new ValidatedFixture {
      import cats.data.Validated._
      import cats.implicits._

      val x = new ValidatedAsApplicative[MyValidated, MyErrorType]
      val success = x.doTry("hello")
      success shouldBe Valid("hello")
    }
  }

}
