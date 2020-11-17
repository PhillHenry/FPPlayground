package uk.co.odinconsultants.fp.cats.traverse

import cats.CommutativeApplicative
import cats.implicits._
import uk.co.odinconsultants.fp.lang.JoyOfSets
import cats.instances.set.catsStdInstancesForSet.unorderedTraverse
import uk.co.odinconsultants.fp.lang.JoyOfSets.MyIntWrapper

object SetsMain {

  def traverseFn[T]: T => List[T]  = { t =>
    println(s"t = $t")
    List(t)
  }

  val commutativeApplicativeSet = new CommutativeApplicative[Set] {
    override def pure[A](x: A): Set[A] = Set(x)

    override def ap[A, B](ff: Set[A => B])(fa: Set[A]): Set[B] = for {
      f <- ff
      a <- fa
    } yield f(a)
  }
  val commutativeApplicativeList = new CommutativeApplicative[List] {
    override def pure[A](x: A): List[A] = List(x)

    override def ap[A, B](ff: List[A => B])(fa: List[A]): List[B] = for {
      f <- ff
      a <- fa
    } yield f(a)
  }

  def main(args: Array[String]): Unit = {

    val listOfSets: List[Set[MyIntWrapper]] = JoyOfSets.set1.unorderedTraverse(traverseFn)(commutativeApplicativeList)
    println(listOfSets)
  }

}
