package uk.co.odinconsultants.fp.cats.applicatives

import cats.Applicative
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.{EitherFixture, ValidatedFixture}
import cats.implicits._
import cats.effect.IO


class ApplicativeVsMonadSpec extends WordSpec with Matchers {

  import ApplicativeVsMonad._

  "Applicatives" should {
    "be composed " in new ValidatedFixture {
      valid1.myApplicativeExtensionMethod(valid2) shouldBe valid2
//      valid1.myMonadExtensionMethod(valid2) does not compile because Validated is not a Monad
    }
  }

  "Monads" should {
    "be composed" in new EitherFixture {
      valid1.myApplicativeExtensionMethod(valid2) shouldBe valid2
      valid1.myMonadExtensionMethod(valid2) shouldBe valid2
    }
  }

}
