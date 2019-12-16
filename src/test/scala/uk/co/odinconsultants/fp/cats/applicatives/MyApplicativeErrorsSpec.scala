package uk.co.odinconsultants.fp.cats.applicatives

import cats.ApplicativeError
import cats.data.Validated
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
@RunWith(classOf[JUnitRunner])
class MyApplicativeErrorsSpec extends WordSpec with Matchers {

  trait EitherFixture {

    type MyAppError[T] = Either[Throwable, T]

    def underTest: MyApplicativeErrors[MyAppError] = {
      import cats.implicits._
      new MyApplicativeErrors[MyAppError]
    }

  }

  "An applicative of Either" should {
    "have a happy path of Right" in new EitherFixture {
      val rightMsg = "a string"
      val pure = underTest.pureHappyPath(rightMsg)
      pure shouldBe Right(rightMsg)
    }
    "have an unhappy path of Left" in new EitherFixture {
      val rightMsg = "a string"
      val x = new Exception(rightMsg)
      val pure = underTest.pureUnhappyPath(x)
      pure shouldBe Left(x)
    }
  }

}
