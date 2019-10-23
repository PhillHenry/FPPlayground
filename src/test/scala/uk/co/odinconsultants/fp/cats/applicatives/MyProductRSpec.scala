package uk.co.odinconsultants.fp.cats.applicatives

import cats.{Applicative, ApplicativeError}
import cats.data.NonEmptyList
import cats.data.Validated.Invalid
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.ValidatedFixture

class MyProductRSpec extends WordSpec with Matchers {

  import MyProductR._

  "Aggregate" should {
    "exploit semigroup behaviour of Validated" in new ValidatedFixture {
      import cats.implicits._
      val result = productR(mixedList)
      println(result)
      result shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
    "exploit semigroup behaviour of Either" ignore new EitherStringFixture {

      import cats.implicits._

      val applicative    = implicitly[Applicative[MyEither]]
      val applicativeErr = implicitly[ApplicativeError[MyEither, String]]


      aggregateEither(mixed) shouldBe Left(failureMsg1 + failureMsg2)
    }
  }

}
