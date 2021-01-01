package uk.co.odinconsultants.fp.cats.kleisli

import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.kleisli.KleisliMonoids._

class KleisliMonoidsSpec extends WordSpec with Matchers {

  "Combining Kleislis" should {
    "act of the monads if they're monoids" in {
      add1Div10.run(90) shouldBe List(91, 9)
    }
  }

}
