package uk.co.odinconsultants.fp.cats.validation

import cats.{Applicative, ApplicativeError}
import cats.data.{EitherNel, NonEmptyChain, NonEmptyList, Validated, ValidatedNec}
import cats.data.Validated.{Invalid, Valid}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
@RunWith(classOf[JUnitRunner])
class ValidatedAsApplicativeSpec extends WordSpec with Matchers {

  trait CreationFixture {
    def create[F[_]: Applicative, X](implicit E: ApplicativeError[F, X]): ValidatedAsApplicative[F, X] = {
      println(s"E = $E")
      new ValidatedAsApplicative[F, X]
    }
  }


  trait EitherNelApplicativeFixture extends CreationFixture {

    type MyErrorType    = String
    type MyDataType     = String
    type MyX            = NonEmptyList[MyErrorType]
    type MyF[T]         = EitherNel[MyErrorType, T]

    def underTest: ValidatedAsApplicative[MyF, MyX] = {
      import cats.implicits._
      create[MyF, MyX]
    }

    def myFailure(x: String): MyF[MyDataType] = Left(NonEmptyList(x, List.empty))

    val valid1: MyF[MyDataType] = Right("valid1")
    val valid2: MyF[MyDataType] = Right("valid2")
    val invalid1Msg = "invalid1Msg"
    val invalid2Msg = "invalid2Msg"
    val invalid1: MyF[String] = myFailure(invalid1Msg)
    val invalid2: MyF[String] = myFailure(invalid2Msg)
  }

  "An applicative of EitherNel" should {
    "have a happy path of Right" in new EitherNelApplicativeFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Right(rightMsg)
    }
    "fail on the first Left" in new EitherNelApplicativeFixture {

      val xs: NonEmptyList[MyF[String]] = NonEmptyList(valid1, List(invalid1, valid2, invalid2))
      val allValidated = underTest.allOrNothing(xs)
      allValidated shouldBe invalid1
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
    //  (note the Semigroup[E] in ats.data.ValidatedInstances.catsDataApplicativeErrorForValidated)
    "implicitly treat String as a semigroup" in new ValidatedApplicativeFixture with ValidatedFixture {
      val allValidated = underTest.allOrNothing(mixedList)
      allValidated shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

  trait ValidatedNecApplicativeFixture extends CreationFixture {

    type MySemiGroup    = NonEmptyChain[String]
    type MyAppError[T]  = ValidatedNec[String, T]

    def underTest: ValidatedAsApplicative[MyAppError, MySemiGroup] = {
      import cats.implicits._
      create[MyAppError, MySemiGroup]
    }
  }

  "An applicative of ValidatedNec" should {
    "have a happy path of Right" in new ValidatedNecApplicativeFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Valid(rightMsg)
    }
  }


}
