package uk.co.odinconsultants.fp.cats.applicatives

import cats.ApplicativeError
import org.scalatest.{Matchers, WordSpec}

class MyApplicativeErrorsSpec extends WordSpec with Matchers {

  "Happy path" should {
    "create Either if the applicative error type" in {
      import cats.implicits._
      type MyAppError[T] = Either[String, T]
//      val io = new MyApplicativeErrors[MyAppError]
    }
  }

  "Either" should {
    "have applicate errors" in {
      import cats.implicits._
      type MyAppError[T] = Either[Throwable, T]
//      implicit val eitherAppException = implicitly[ApplicativeError[MyAppError, Throwable]]
      val io = new MyApplicativeErrors[MyAppError]
    }
  }

}
