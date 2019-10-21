package uk.co.odinconsultants.fp.cats.validation

import cats.data.{EitherNel, NonEmptyList}
import org.scalatest.{Matchers, WordSpec}

class SemigroupApplicativeErrorSpec extends WordSpec with Matchers {

  trait EitherNelApplicativeFixture {

    type MyErrorType    = String
    type MyDataType     = String
    type MyX            = NonEmptyList[MyErrorType]
    type MyF[T]         = EitherNel[MyErrorType, T]

    def underTest: SemigroupApplicativeError[MyF, MyX] = {
      import cats.implicits._
      new SemigroupApplicativeError[MyF, MyX]
    }

    def myFailure(x: String): MyF[MyDataType] = Left(NonEmptyList(x, List.empty))

    val valid1: MyF[MyDataType] = Right("valid1")
    val valid2: MyF[MyDataType] = Right("valid2")
    val invalid1Msg = "invalid1Msg"
    val invalid2Msg = "invalid2Msg"
    val invalid1: MyF[String] = myFailure(invalid1Msg)
    val invalid2: MyF[String] = myFailure(invalid2Msg)
  }

  "An applicative of EitherNel" should {
    "fail on the first Left" in new EitherNelApplicativeFixture {
      val xs: NonEmptyList[MyF[String]] = NonEmptyList(valid1, List(invalid1, valid2, invalid2))
      val allValidated = underTest.allOrNothing(xs)
      allValidated shouldBe myFailure(invalid1Msg +  invalid2Msg)
    }
  }
}
