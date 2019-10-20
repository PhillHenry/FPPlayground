package uk.co.odinconsultants.fp.cats.bifunctor

import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.EitherFixture

class MyBifunctorSpec extends WordSpec with Matchers {

  import MyBifunctor._

  "Bifunctor" should {

    "be agnostic as to whether it's an Either (Left)" in new EitherFixture {
      import cats.implicits._
      agnostic(invalid1) shouldBe Left(MyException(invalid1Msg))
    }
    "be agnostic as to whether it's an Either (Right)" in new EitherFixture {
      import cats.implicits._
      agnostic(valid1) shouldBe Right(biMappedStr(invalid1Msg))
    }

  }

}
