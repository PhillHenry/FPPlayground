package uk.co.odinconsultants.fp.cats.validation

import cats.implicits._
import org.scalatest.{Matchers, WordSpec}

class ValidatorSpec extends WordSpec with Matchers {

  trait ValidatorFixture extends ValidationFixture {
    val o1 = Option(first)
    val o2 = Option(second)
    val o3 = Option(third)
    val validatesOption = new Validator(o1, o2, o3)

    type MyEither = Either[Exception, String]
    val r1: MyEither = Right(first)
    val r2: MyEither = Right(second)
    val r3: MyEither = Right(third)
    val validatesEither = new Validator(r1, r2, r3)
  }

  "Options" should {
    "return something monadic for the happy path" in new ValidatorFixture {
      validatesOption.doMonads shouldBe Some(expectedStr)
    }
    "return something applicative for the happy path" in new ValidatorFixture {
      validatesOption.doApplicatives shouldBe Some(expectedStr)
    }
  }

  "Eithers" should {
    "return something monadic for the happy path" in new ValidatorFixture {
      validatesEither.doMonads shouldBe Right(expectedStr)
    }
    "return something applicative for the happy path" in new ValidatorFixture {
      validatesEither.doApplicatives shouldBe Right(expectedStr)
    }
  }

  "Monads" should {
    "be applicatives" in new ValidatorFixture {
      validatesOption.doApplicatives shouldBe Some(expectedStr)
    }
  }

}
