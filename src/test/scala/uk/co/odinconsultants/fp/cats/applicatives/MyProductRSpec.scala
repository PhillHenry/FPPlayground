package uk.co.odinconsultants.fp.cats.applicatives

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
    "exploit semigroup behaviour of Either" ignore {
      type MyEither[T] = Either[String, T]

      val success1: MyEither[String] = Right("success1")
      val success2: MyEither[String] = Right("success2")
      val failureMsg1 = "failure1"
      val failureMsg2 = "failure2"
      val failure1: MyEither[String] = Left(failureMsg1)
      val failure2: MyEither[String] = Left(failureMsg2)

      import cats.implicits._
      val result = aggregateEither(NonEmptyList.of(success1, failure1, success2, failure2))
      result shouldBe Left(failureMsg1 + failureMsg2)
    }
  }

}
