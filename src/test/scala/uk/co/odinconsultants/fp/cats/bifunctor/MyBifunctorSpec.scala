package uk.co.odinconsultants.fp.cats.bifunctor

import cats.data.Validated.{Invalid, Valid}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.{EitherFixture, ValidatedFixture}
@RunWith(classOf[JUnitRunner])
class MyBifunctorSpec extends WordSpec with Matchers {

  import MyBifunctor._

  "Bifunctor" should {

    "be agnostic as to whether it's an Either (Left)" in new EitherFixture {
      import cats.implicits._
      agnostic(invalid1) shouldBe Left(MyException(invalid1Msg))
    }
    "be agnostic as to whether it's an Either (Right)" in new EitherFixture {
      import cats.implicits._
      agnostic(valid1) shouldBe Right(biMappedStr(valid1Msg))
    }

    "be agnostic as to whether it's an Validated (Invalid)" in new ValidatedFixture {
      agnostic(invalidThrowable1) shouldBe Invalid(MyException(invalid1Msg))
    }
    "be agnostic as to whether it's an Validated (Valid)" in new ValidatedFixture {
      agnostic(validThrowable1) shouldBe Valid(biMappedStr(valid1Msg))
    }

  }

}
