package uk.co.odinconsultants.fp.cats.ana_cata

import org.scalatest.{Matchers, WordSpec}

import cats.implicits._

class MyCofreeSpec extends WordSpec with Matchers {

  import MyCofree._

  "Cofree" should {
    "be able to be turned to a list" in {
      unfoldedHundred.toList should have size 101 // (0, 100) has length 101
    }
  }

}
