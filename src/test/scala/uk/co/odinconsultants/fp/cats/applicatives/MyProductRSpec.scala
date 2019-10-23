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

      //implicit val applicative    = implicitly[Applicative[MyEither]]
      //implicit val applicativeErr = implicitly[ApplicativeError[MyEither, String]]

      // No, beacuse that's the difference between Either and Validated
      // It's all down to the `product` function
      // See https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/data/Validated.scala#L516-L522
      productR(mixed) shouldBe Left(failureMsg1 + failureMsg2)
    }
  }

}
