package uk.co.odinconsultants.fp.cats.validation

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import org.scalatest.{Matchers, WordSpec}

class MyValidatedNecSpec extends WordSpec with Matchers {
  import MyValidatedNec._
  "Aggregating" should {
    "produce only that Throwables messages" in {
      type MyVNec                         = ValidatedNec[Throwable, String]
      val failureMsg1                     = "fail1"
      val failureMsg2                     = "fail2"
      val bad:    MyVNec                  = Invalid(NonEmptyChain.one(new Throwable(failureMsg1)))
      val bad2:   MyVNec                  = Invalid(NonEmptyChain.one(new Throwable(failureMsg2)))
      val good:   MyVNec                  = Valid("success")
      val mixed:  NonEmptyChain[MyVNec]   = NonEmptyChain(good, good, bad, bad2, good)

      val result = aggregate(mixed)

      result shouldBe Invalid("\n" + concatFn(failureMsg1, failureMsg2))
    }
  }

}
