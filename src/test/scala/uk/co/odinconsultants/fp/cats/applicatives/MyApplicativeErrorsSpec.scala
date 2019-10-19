package uk.co.odinconsultants.fp.cats.applicatives

import cats.ApplicativeError
import org.scalatest.{Matchers, WordSpec}

class MyApplicativeErrorsSpec extends WordSpec with Matchers {

  "Happy path" should {
    "create Either if the applicative error type" in {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Right(rightMsg)
    }
  }

  "Unhappy path" should {
    "create Either if that's the applicative error type" in {
      val rightMsg = "a string"
      val x = new Exception(rightMsg)
      val pure = underTest.pureUnhappyPath(x)
      pure shouldBe Left(x)
    }
  }

  type MyAppError[T] = Either[Throwable, T]

  "Either" should {
    "have applicate errors" in {
//      implicit val eitherAppException = implicitly[ApplicativeError[MyAppError, Throwable]]
    }
  }

  def underTest: MyApplicativeErrors[MyAppError] = {
    import cats.implicits._
    new MyApplicativeErrors[MyAppError]
  }

}
