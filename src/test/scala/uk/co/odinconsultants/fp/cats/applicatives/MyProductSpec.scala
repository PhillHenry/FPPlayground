package uk.co.odinconsultants.fp.cats.applicatives

import org.scalatest.{Matchers, WordSpec}
import cats.implicits._

class MyProductSpec extends WordSpec with Matchers {

  import MyProduct._

  "mapN" should {
    "act on the internals of a container as a Cartesian product" in {
      myProduct(List(1, 2, 3), List(10, 11, 12)) shouldBe List((1, 10), (1, 11), (1, 12), (2, 10), (2, 11), (2, 12), (3, 10), (3, 11), (3, 12))
    }
  }

}
