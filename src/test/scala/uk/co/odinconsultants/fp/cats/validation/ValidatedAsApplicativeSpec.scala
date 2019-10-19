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

  trait EitherApplicativeFixture extends CreationFixture {

    type MyX            = Throwable
    type MyAppError[T]  = Either[MyX, T]

    def underTest: ValidatedAsApplicative[MyAppError, MyX] = {
      import cats.implicits._
      create[MyAppError, MyX]
    }

  }

  "An applicative of Either" should {
    "have a happy path of Right" in new EitherApplicativeFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Right(rightMsg)
    }
    "fail on the first Left" in new EitherApplicativeFixture with EitherFixture {
      val allValidated = underTest.allOrNothing(mixedList)
      allValidated shouldBe Left(throwable1)
    }
  }

  trait ValidatedApplicativeFixture extends CreationFixture {

    type MySemiGroup    = String
    type MyAppError[T]  = Validated[MySemiGroup, T]

    def underTest: ValidatedAsApplicative[MyAppError, MySemiGroup] = {
      import cats.implicits._
      create[MyAppError, MySemiGroup]
    }
  }

  "An applicative of Validated" should {
    "have a happy path of Right" in new ValidatedApplicativeFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Valid(rightMsg)
    }
    "implicitly treat String as a semigroup" in new ValidatedApplicativeFixture with ValidatedFixture {
      val allValidated = underTest.allOrNothing(mixedList)
      allValidated shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

}
