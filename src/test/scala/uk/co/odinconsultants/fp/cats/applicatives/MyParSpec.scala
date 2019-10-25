package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.EitherNec
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.{EitherFixture, ValidatedFixture}

class MyParSpec extends WordSpec with Matchers {

  import MyPar._

  "par" should {
    "accumulate errors for Validated" in new ValidatedFixture {
      parToAccumulateErrorsInValidated(valid1, valid2, valid3)
    }
    "accumulate errors for Either" in {
      type MyEither = EitherNec[String, String]
      val valid1: MyEither = Right("success!")
      val valid2: MyEither = Right("success!")
      val valid3: MyEither = Right("success!")
      val accumulated: EitherNecAcc = parToAccumulateErrorsInEitherNec(valid1, valid2, valid3)
      println(accumulated)
    }
  }

}
