package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness

object RefinedMatrix {

  type Index = Int Refined Greater[W.`0`.T]

  case class MyMatrix[X <: Index, Y <: Index](height: X, width: Y) {
    val X =  Witness.mkWitness(width)

//    def multiply[Z <: Index](other: MyMatrix[Int Refined Equal[X.T], Z]): MyMatrix[X, Z] = MyMatrix(height, other.width)
  }

  def main(args: Array[String]): Unit = {
    val _3: Index = 3
    val exactly7: Int Refined Equal[W.`7`.T] = 7
    val _7: Index = 7
    //    val illegal = MyMatrix(-1, -1)
    val a = MyMatrix(_3, _7)
    val b = MyMatrix(11: Index, 13: Index)
//    val c = MyMatrix(exactly7, exactly7)

//    a.multiply(b) // illegal
//    a.multiply(c)

    val positiveInt: Index = 5
    val mustBe3 = Witness.mkWitness(_3)
//    val mustEqual: Int Refined Equal[mustBe3.T] = 3

    def print3(x: Int Refined Equal[mustBe3.T]): Unit = println(x)

//    print3(3)
//    print3(_3)

    println(a)
  }

}
