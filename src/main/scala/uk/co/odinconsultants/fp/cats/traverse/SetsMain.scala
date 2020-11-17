package uk.co.odinconsultants.fp.cats.traverse

import cats.CommutativeApplicative
import cats.implicits._

object SetsMain {

  val commutativeApplicativeList = new CommutativeApplicative[List] {
    override def pure[A](x: A): List[A] = List(x)

    override def ap[A, B](ff: List[A => B])(fa: List[A]): List[B] = for {
      f <- ff
      a <- fa
    } yield f(a)
  }

  def main(args: Array[String]): Unit = {
    val setOfLists: Set[List[Int]] = Set(1, 2, 3, 4, 5).map(List(_))
    val listOfSets: List[Set[Int]] = setOfLists.unorderedTraverse(identity[List[Int]])(commutativeApplicativeList)
    println(setOfLists)
    println(listOfSets)
  }

}
