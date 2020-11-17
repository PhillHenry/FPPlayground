package uk.co.odinconsultants.fp.cats.traverse

import cats.implicits._
import uk.co.odinconsultants.fp.lang.JoyOfSets
import cats.instances.set.catsStdInstancesForSet.unorderedTraverse
import uk.co.odinconsultants.fp.lang.JoyOfSets.MyIntWrapper

object SetsMain {

  def traverseFn[T]: T => Set[T]  = { t =>
    println(s"t = $t")
    Set(t)
  }

  def main(args: Array[String]): Unit = {
//    implicit val ev = unorderedTraverse[MyIntWrapper]
//    JoyOfSets.set1.unorderedTraverse(traverseFn)
  }

}
