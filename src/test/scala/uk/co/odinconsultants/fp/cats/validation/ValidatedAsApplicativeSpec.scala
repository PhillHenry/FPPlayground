package uk.co.odinconsultants.fp.cats.validation

import cats.data.Validated
import cats.data.Validated.Valid
import org.scalatest.{Matchers, WordSpec}

class ValidatedAsApplicativeSpec extends WordSpec with Matchers {

  trait EitherFixture {

    type MyErrorType    = Throwable
    type MyAppError[T]  = Either[MyErrorType, T]

    def underTest: ValidatedAsApplicative[MyAppError, MyErrorType] = {
      import cats.implicits._
      new ValidatedAsApplicative[MyAppError, MyErrorType]
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

  trait ValidatedFixture {

    type MyErrorType    = String
    type MyAppError[T]  = Validated[MyErrorType, T]

    def underTest: ValidatedAsApplicative[MyAppError, MyErrorType] = {
      import cats.implicits._
      new ValidatedAsApplicative[MyAppError, MyErrorType]
    }
  }

  "An applicative of Validated" should {
    "have a happy path of Right" in new ValidatedFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Valid(rightMsg)
    }
  }

}
