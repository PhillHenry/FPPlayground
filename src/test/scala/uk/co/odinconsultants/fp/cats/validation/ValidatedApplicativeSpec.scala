package uk.co.odinconsultants.fp.cats.validation

import cats.{Applicative, ApplicativeError}
import cats.data.{EitherNel, NonEmptyChain, NonEmptyList, Validated, ValidatedNec}
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{Matchers, WordSpec}

class ValidatedApplicativeSpec extends WordSpec with Matchers {

  "ValidatedApplicative" should {
    type MySemiGroup    = String
    type MyAppError[T]  = Validated[MySemiGroup, T]
    "create Validated" in {
//      new ValidatedApplicative[MyAppError, String]
    }
  }

}
