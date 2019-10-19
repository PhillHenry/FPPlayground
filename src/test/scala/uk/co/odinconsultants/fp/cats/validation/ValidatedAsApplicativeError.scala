package uk.co.odinconsultants.fp.cats.validation

import cats.ApplicativeError
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.applicatives.MyApplicativeErrors

class ValidatedAsApplicativeError extends WordSpec with Matchers {

  trait ValidatedAsApplicativeFixture {
    type MyDataType     = String
    type MyErrorType    = String
    type MyValidated[T] = Validated[MyErrorType, T]
    val invalid1Msg     = "invalid"
    val invalid2Msg     = "invalid2"
    val valid1          = Valid("valid1")
    val invalid1        = Invalid(invalid1Msg)
    val valid2          = Valid("valid2")
    val invalid2        = Invalid(invalid2Msg)
    val valid3          = Valid("valid3")
    val mixedList: NonEmptyList[Validated[MyErrorType, MyDataType]] = new NonEmptyList(valid1, List(invalid1, valid2, invalid2, valid3))
  }

  "Validated" should {
    "have an ApplicatativeError if its type has a semigroup" in new ValidatedAsApplicativeFixture {
      import cats.data.Validated._
      import cats.implicits._

      val x = new ValidatedAsApplicative[MyValidated, MyErrorType]

      val allValidated = x.allOrNothing(mixedList)
      println(s"allOrNothing = $allValidated")
      allValidated shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

  "Happy path execution" should {
    "produce a F representing success" in new ValidatedAsApplicativeFixture {
      import cats.data.Validated._
      import cats.implicits._

      val x = new ValidatedAsApplicative[MyValidated, MyErrorType]
      val success = x.doTry("hello")
      success shouldBe Valid("hello")
    }
  }

}
