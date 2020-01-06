package uk.co.odinconsultants.fp.cats.tupled

import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.tupled.MyMapN._
import cats.implicits._

class MyMapNSpec extends WordSpec with Matchers {

  "mapN" should {
    "act on the internals of a container as a Cartesian product" in {
      myMapN(List(1, 2, 3), List(10, 11, 12)) shouldBe List(11, 12, 13, 12, 13, 14,13, 14, 15)
    }
  }

}
