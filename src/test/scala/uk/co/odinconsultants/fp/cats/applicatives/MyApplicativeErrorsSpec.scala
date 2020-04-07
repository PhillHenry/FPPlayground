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
  "An applicative of Option" should {
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

  /**
   * This will not work for Option though : instances of these constructs do not exist for Options
   * (the reason becomes obvious if you think 5 minutes about it).
   * https://www.reddit.com/r/scala/comments/bkxnd8/partial_functions_with_hkt_higher_kinded_types/
   *
   * "Do note that by using this instance you will be throwing away some information and behaviour."
   * because None carries no further information.
   * https://rubenpieters.github.io/applicativeerror/cats/2017/01/20/applicativeerror-1.html
   */
  "An option" should {
    "yield a None" in {
      import cats.implicits._
      import cats.implicits.catsStdInstancesForOption._
//      val appErr = MyApplicativeErrors.myRaiseError[Option, String]("test")
//      appErr match {
//        case Some(_) => fail("Got something")
//        case None => "ok"
//      }
    }
  }

}
