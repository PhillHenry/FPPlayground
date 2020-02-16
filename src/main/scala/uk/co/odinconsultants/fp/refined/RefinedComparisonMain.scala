package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.{Positive, _}
import shapeless.Witness

object RefinedComparisonMain {

  case class MySimpleCase[X](x: X) {
    val X =  Witness.mkWitness(x)

    def onlyAcceptSelf(x: MySimpleCase[X.T]): Unit = println("OK")

  }

  case class MyCaseClass[X <: Int Refined Positive](x: X)

  case class MyExactCase[X <: Int Refined Equal[X]](x: X)

//  def makeSimple(x: Int): MySimpleCase[Int Refined Equal[Int]] = {
//    val value: Refined[Int, Equal[Int]] = refineMV[Equal[Int]](x)
//    MySimpleCase(value)
//  }

  def main(args: Array[String]): Unit = {
    val positive5: Int Refined Positive = 5
    val casePositive5 = MyCaseClass(positive5)

    type Exactly[T] = Int Refined Equal[T]

    val exactly5: Exactly[W.`5`.T] = 5
    val exactly7: Exactly[W.`7`.T] = 7
//    val caseExactly5 = MyCaseClass(exactly5) <-- Type mismatch

//    val exactCase5 = MyExactCase(exactly5) // Equal[Int(5)] does not conform to Equal[X]

    val simple5: MySimpleCase[Exactly[W.`5`.T]] = MySimpleCase(exactly5)
    val simple7: MySimpleCase[Exactly[W.`7`.T]] = MySimpleCase(exactly7)
    simple5.onlyAcceptSelf(simple5)
    simple5.onlyAcceptSelf(MySimpleCase(5))
//    simple5.onlyAcceptSelf(MySimpleCase(6)) // <-- doesn't compile, exactly as we desire!
//    simple5.onlyAcceptSelf(simple7)         // <-- doesn't compile, exactly as we desire!
  }

}
