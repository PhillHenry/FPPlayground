package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.{Positive, _}
import shapeless.Witness
import shapeless.Witness.Aux

import scala.reflect.runtime.universe._

object RefinedComparisonMain {

  case class MySimpleCase[X](x: X) {
    val X =  Witness.mkWitness(x)

    def onlyAcceptSelf(x: MySimpleCase[X.T]): Unit = println("OK")

  }

  case class MyCaseClass[X <: Int Refined Positive](x: X)

  case class MyExactCase[X <: Int Refined Equal[X]](x: X)

  class MyTagCase2[X <: Int Refined Equal[_]: TypeTag] {
    val targs = typeOf[X]
    println(s"targs = ${targs}")
  }


  class MyTagCase[X <: Int Refined Equal[Y]: TypeTag, Y: TypeTag] {
    val t = typeOf[X]
    val targs = typeOf[X] match { case TypeRef(_, _, args) => args }
    println(s"targs = $t [ ${t.getClass} ] with args $targs")
  }

  type Exactly[T] = Int Refined Equal[T]

  def main(args: Array[String]): Unit = {
    val positive5: Int Refined Positive = 5
    val casePositive5 = MyCaseClass(positive5)


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

    val x = W.`5`
    println(s"${W.`5`.getClass}")

    new MyTagCase[Int Refined Equal[W.`5`.T], W.`5`.T]
  }

  // http://tpolecat.github.io/2015/07/30/infer.html
//  final class WrapHelper[F[_]] {
//    def apply[A](a: A)(implicit ev: Applicative[F]): F[A] =
//      ev.point(a)
//  }
//  final class WrapHelper[F[_]] {
//    def apply[A](a: A)(implicit ev: Exactly[F]): F[A] =
//      refineMV[Exactly[A]](a)
//  }

}
