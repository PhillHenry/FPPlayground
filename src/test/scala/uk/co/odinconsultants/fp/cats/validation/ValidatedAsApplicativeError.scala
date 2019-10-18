package uk.co.odinconsultants.fp.cats.validation

import cats.ApplicativeError
import cats.data.Validated
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.applicatives.MyApplicativeErrors

class ValidatedAsApplicativeError extends WordSpec with Matchers {

  "Validated" should {
    "have an ApplicatativeError if its type has a semigroup" in {
      import cats.data._
      import cats.data.Validated._
      import cats.implicits._

      type MyErrorType = String
      type MyValidated[T] = Validated[MyErrorType, T]

      val applicativeError = implicitly[ApplicativeError[MyValidated, MyErrorType]]

      val x = new ValidatedAsApplicative[MyValidated, MyErrorType]
    }
  }

}
