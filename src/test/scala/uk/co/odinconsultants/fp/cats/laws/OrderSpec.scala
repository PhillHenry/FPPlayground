package uk.co.odinconsultants.fp.cats.laws

import org.scalatest.{Matchers, WordSpec}

import cats.implicits._

class OrderSpec extends WordSpec with Matchers {

  "Float laws" should {
    "obey... er something" in {
      val isEqNan     = cats.kernel.laws.discipline.OrderTests[Float].laws.partialCompare(Float.NaN, Float.NaN)
      val isEqPosNeg  = cats.kernel.laws.discipline.OrderTests[Float].laws.partialCompare(-0f,0f)
      val isEqSame    = cats.kernel.laws.discipline.OrderTests[Float].laws.partialCompare(1, 1)
      println(isEqNan)    // IsEq(false,true)
      println(isEqPosNeg) // IsEq(false,true)
      println(isEqSame)   // IsEq(true,true)
    }
  }

}
