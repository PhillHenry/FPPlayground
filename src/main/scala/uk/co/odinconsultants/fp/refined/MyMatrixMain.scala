package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness
import shapeless.Witness.Aux

import scala.reflect.runtime.universe._

case class Matrix2[ROWS <: ExactInt, COLS  <: ExactInt](nRows: ROWS, nCols: COLS) {

  val REQUIRED_COLS =  Witness.mkWitness(nCols)

  def multiply[T <: ExactInt](x: Matrix2[REQUIRED_COLS.T, T]): Matrix2[ROWS, T] = Matrix2(nRows, x.nCols)

}

case class Matrix[ROWS, COLS](nRows: ROWS, nCols: COLS) {

  val REQUIRED_COLS =  Witness.mkWitness(nCols)

  def multiply[T](x: Matrix[REQUIRED_COLS.T, T]): Matrix[ROWS, T] = Matrix(nRows, x.nCols)

}

object MyMatrixMain {
  type Exactly[T] = Int Refined Equal[T]

  def main(args: Array[String]): Unit = {
    val x: Matrix[Exactly[W.`3`.T], Exactly[W.`7`.T]]   = Matrix(3: Exactly[W.`3`.T], 7: Exactly[W.`7`.T])
    val x2                                              = Matrix(3: Exactly[W.`3`.T], 7: Exactly[W.`7`.T])
    val y                                               = Matrix(7: Exactly[W.`7`.T], 5: Exactly[W.`5`.T])
    val y2                                              = Matrix(7, 5)
    val y3: Matrix[Exactly[W.`7`.T], Exactly[W.`5`.T]]  = Matrix(7, 5)
    val z                                               = Matrix(13: Exactly[W.`13`.T], 11: Exactly[W.`11`.T])
    x.multiply(y)
    x2.multiply(y)
//    x.multiply(y2)            // not sure why this doesn't compile but this below:
    x.multiply(y3)              // does
    x.multiply(Matrix(7, 5))    // and this too
    x2.multiply(Matrix(7, 5))   //
//    x.multiply(z)             // doesn't compile as expected

    val _3x7: Matrix2[Exactly[W.`3`.T], Exactly[W.`7`.T]]   = Matrix2(3: Exactly[W.`3`.T], 7: Exactly[W.`7`.T])
    val _7x8: Matrix2[Exactly[W.`7`.T], Exactly[W.`8`.T]]   = Matrix2(7: Exactly[W.`7`.T], 8: Exactly[W.`8`.T])
    val _8x7: Matrix2[Exactly[W.`8`.T], Exactly[W.`7`.T]]   = Matrix2(8: Exactly[W.`8`.T], 7: Exactly[W.`7`.T])
    _3x7.multiply(_7x8)
//    _3x7.multiply(_8x7) // doesn't compile as expected
  }

}
