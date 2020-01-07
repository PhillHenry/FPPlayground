package uk.co.odinconsultants.fp.cats.applicatives

import org.scalatest.{Matchers, WordSpec}
import cats.implicits._
import uk.co.odinconsultants.fp.DataStructures

class MyProductSpec extends WordSpec with Matchers {

  import MyProduct._

  "mapN" should {
    "produce a Cartesian product" in new DataStructures {
      myProduct(oneToThreeInc, tenToTwelveInc) shouldBe cartesianProduct1to3And10to12
    }
  }

}
