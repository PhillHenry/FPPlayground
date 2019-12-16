package uk.co.odinconsultants.fp.cats.validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
@RunWith(classOf[JUnitRunner])
class ValidatorSpec extends WordSpec with Matchers {

  "Options" should {
    "return something monadic for the happy path" in new ValidatorFixture {
      validatesOption.doMonads shouldBe Some(happyPath)
    }
    "return something applicative for the happy path" in new ValidatorFixture {
      validatesOption.doApplicatives shouldBe Some(happyPath)
    }
  }

  "Eithers" should {
    "return something monadic for the happy path" in new ValidatorFixture {
      validatesEither.doMonads shouldBe Right(happyPath)
    }
    "return something applicative for the happy path" in new ValidatorFixture {
      validatesEither.doApplicatives shouldBe Right(happyPath)
    }
  }

  "Monads" should {
    "be applicatives" in new ValidatorFixture {
      validatesOption.doApplicatives shouldBe validatesOption.doMonads
    }
  }

}
