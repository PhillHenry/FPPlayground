package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.{EitherNec, NonEmptyChain}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.{EitherFixture, ValidatedFixture}
@RunWith(classOf[JUnitRunner])
class MyParSpec extends WordSpec with Matchers {

  import MyPar._

  trait EitherStringFixture {
    type MyEither = EitherNec[String, String]
    val success1Msg = "success1!"
    val success2Msg = "success2!"
    val success3Msg = "success3!"
    val valid1: MyEither = Right(success1Msg)
    val valid2: MyEither = Right(success2Msg)
    val valid3: MyEither = Right(success3Msg)

    import cats.data._

    val failure1Msg = "failure1"
    val failure2Msg = "failure2"
    val invalid1: MyEither = Left(NonEmptyChain(failure1Msg))
    val invalid2: MyEither = Left(NonEmptyChain(failure2Msg))
  }

  "par" should {
    "accumulate errors for Validated" in new ValidatedFixture {
      parToAccumulateErrorsInValidated(valid1, valid2, valid3)
    }
    "accumulate success for Either" in new EitherStringFixture {
      val accumulated: EitherNecAcc = parToAccumulateErrorsInEitherNec(valid1, valid2, valid3)
      accumulated shouldBe Right(concatStrings(success1Msg, success2Msg, success3Msg))
    }
    "accumulate errors" in new EitherStringFixture {
      val accumulated: EitherNecAcc = parToAccumulateErrorsInEitherNec(valid1, invalid1, invalid2)
      accumulated shouldBe Left(NonEmptyChain(failure1Msg, failure2Msg))
    }
  }

}
