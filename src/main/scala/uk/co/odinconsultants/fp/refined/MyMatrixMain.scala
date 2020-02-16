package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness
import shapeless.Witness.Aux

import scala.reflect.runtime.universe.TypeTag

case class Matrix[ROWS, COLS](nRows: ROWS, nCols: COLS) {
  val ROWS =  Witness.mkWitness(nRows)
  val COLS =  Witness.mkWitness(nCols)

  def multiply[T](x: Matrix[COLS.T, T]): Matrix[ROWS, T] = {
    println("OK")
    Matrix(nRows, x.nCols)
  }

}

object MyMatrixMain {
  type Exactly[T] = Int Refined Equal[T]

  def main(args: Array[String]): Unit = {
    val x: Matrix[Exactly[W.`3`.T], Exactly[W.`7`.T]] = Matrix(3: Exactly[W.`3`.T], 7: Exactly[W.`7`.T])
    val y   = Matrix(7: Exactly[W.`7`.T], 5: Exactly[W.`5`.T])
    val y2  = Matrix(7, 5)
    val z   = Matrix(13: Exactly[W.`13`.T], 11: Exactly[W.`11`.T])
    x.multiply(y)
//    x.multiply(y2)          // not sure why this doesn't compile but this below:
    x.multiply(Matrix(7, 5))  // does compile...
//    x.multiply(z)           // doesn't compile as expected
  }

}
