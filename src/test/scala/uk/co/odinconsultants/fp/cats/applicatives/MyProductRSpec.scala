package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.Validated.Invalid
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.validation.ValidatedFixture

class MyProductRSpec extends WordSpec with Matchers {

  import MyProductR._

  "Aggregate" should {
    "exploit semigroup behaviour of Either" in new ValidatedFixture {
      val result = aggregate(mixedList)
      println(result)
      result shouldBe Invalid(invalid1Msg + invalid2Msg)
    }
  }

}
