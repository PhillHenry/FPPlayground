package uk.co.odinconsultants.fp.shapeless.numerical

import shapeless.Witness
import shapeless.syntax.singleton._

object NumericalMain {

  class SillyMatrix[U, V]//(u: U, v: V) {}

  val (wTrue, wFalse) = (Witness(true), Witness(false))

  def main(args: Array[String]): Unit = {
    val w0 = Witness(0)
    type _0 = w0.T

    val w42 = Witness(42)
    type _42 = w42.T

    val w18 = Witness(18)
    type _18 = w18.T

    val w3 = Witness(3)
    type _3 = w3.T

    val a = new SillyMatrix[_42, _18]//(w42.value, w18.value)
    val b = new SillyMatrix[_18, _3]
    val c = new SillyMatrix[_0, _3]

    println(42.narrow)
    println(w3.value)
    println(w3.getClass)

    sillyMatrixMultiply(a, b)
//    sillyMatrixMultiply(a, c) // this doesn't compile - as indeed should be the case!
  }

  def sillyMatrixMultiply[X, Y, Z](a: SillyMatrix[X, Y], b: SillyMatrix[Y, Z]): Unit =
    println("Everything appears fine")

}
