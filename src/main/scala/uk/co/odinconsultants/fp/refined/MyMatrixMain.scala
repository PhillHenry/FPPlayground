package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness
import shapeless.Witness.Aux

import scala.reflect.runtime.universe.TypeTag

case class MyMatrix[X, Y](x: X, y: Y)



object MakeMatrix {

  type Index = Int Refined Greater[W.`0`.T]

  type Exactly[T] = Int Refined Equal[T]

  def apply(x: Int, y: Int) = {
    val X: Aux[Int] =  Witness.mkWitness(x)
    val Y: Aux[Int] =  Witness.mkWitness(x)

    type ExactlyX = Int Refined Equal[X.T]
    type ExactlyY = Int Refined Equal[Y.T]

//    val exactlyX: ExactlyX = refineMV[ExactlyX](x)
//    val exactlyY: ExactlyY = y
//
//    val m: MyMatrix[ExactlyY, ExactlyY] = MyMatrix(exactlyX, exactlyY)
  }

  class MyMatrixWTypes[X <: Exactly[_]: TypeTag, Y <: Exactly[_]] {
  }

}

object MyMatrixMain {



  def main(args: Array[String]): Unit = {

  }

}
