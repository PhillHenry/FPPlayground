package uk.co.odinconsultants.fp.cats.tupled

import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.tupled.MyMapN._
import cats.implicits._
import uk.co.odinconsultants.fp.DataStructures

class MyMapNSpec extends WordSpec with Matchers {

  "mapN" should {
    "act on the internals of a container as a Cartesian product" in new DataStructures {
      myMapN(oneToThreeInc, tenToTwelveInc) shouldBe List(11, 12, 13, 12, 13, 14, 13, 14, 15)
    }
    "ignore surplus elements" in new DataStructures {
      myMapN(oneToThreeInc, tenToTwelveInc.take(2)) shouldBe List(11, 12, 12, 13, 13, 14)
    }
  }

  def toSome[T](xs: List[T]): List[Option[T]] = xs.map(x => Some(x))

  "mapN and product" should {
    "be both Cartesian and effectful" in new DataStructures {
      mapNAndProduct(toSome(oneToThreeInc), toSome(tenToTwelveInc)) shouldBe toSome(cartesianProduct1to3And10to12)
    }
  }

}
