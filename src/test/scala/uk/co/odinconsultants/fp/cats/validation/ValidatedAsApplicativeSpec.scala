package uk.co.odinconsultants.fp.cats.validation

import cats.{Applicative, ApplicativeError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{Matchers, WordSpec}

class ValidatedAsApplicativeSpec extends WordSpec with Matchers {

  trait CreationFixture {
    def create[F[_]: Applicative, X](implicit E: ApplicativeError[F, X]): ValidatedAsApplicative[F, X] = {
      new ValidatedAsApplicative[F, X]
    }
  }

  trait EitherFixture extends CreationFixture {

    type MyErrorType    = Throwable
    type MyAppError[T]  = Either[MyErrorType, T]

    def underTest: ValidatedAsApplicative[MyAppError, MyErrorType] = {
      import cats.implicits._
      create[MyAppError, MyErrorType]
    }

  }

  "An applicative of Either" should {
    "have a happy path of Right" in new EitherFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Right(rightMsg)
    }
//    "have an unhappy path of Left" in new EitherFixture {
//      val rightMsg = "a string"
//      val x = new Exception(rightMsg)
//      val pure = underTest.pureUnhappyPath(x)
//      pure shouldBe Left(x)
//    }
  }

  trait ValidatedFixture extends CreationFixture {

    type MySemiGroup    = String
    type MyAppError[T]  = Validated[MySemiGroup, T]

    def underTest: ValidatedAsApplicative[MyAppError, MySemiGroup] = {
      import cats.implicits._
      create[MyAppError, MySemiGroup]
    }
  }

  "An applicative of Validated" should {
    "have a happy path of Right" in new ValidatedFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Valid(rightMsg)
    }
    "implicitly treat String as a semigroup" in new ValidatedAsApplicativeFixture with ValidatedFixture {
      val allValidated = underTest.allOrNothing(mixedList)
      println(s"allOrNothing = $allValidated")
      allValidated shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

}
