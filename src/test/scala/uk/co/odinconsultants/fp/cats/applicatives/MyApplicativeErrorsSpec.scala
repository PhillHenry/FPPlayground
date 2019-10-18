package uk.co.odinconsultants.fp.cats.applicatives

import cats.ApplicativeError
import org.scalatest.{Matchers, WordSpec}

class MyApplicativeErrorsSpec extends WordSpec with Matchers {

  "Either" should {
    "have applicate errors" in {
      import cats.implicits._
      type MyAppError[T] = Either[Exception, T]
//      implicit val eitherAppException = implicitly[ApplicativeError[MyAppError, _]]
//      val io = new MyApplicativeErrors[MyAppError]
    }
  }

}
