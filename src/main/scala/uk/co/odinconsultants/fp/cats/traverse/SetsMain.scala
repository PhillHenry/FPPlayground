package uk.co.odinconsultants.fp.cats.traverse

import cats.CommutativeApplicative
import cats.implicits._

/**
 * @see https://stackoverflow.com/questions/53945996/cats-effecthow-to-transform-mapx-ioy-to-iomapx-y
 */
object SetsMain {

  val commutativeApplicativeList = new CommutativeApplicative[List] {
    override def pure[A](x: A): List[A] = {
      println(s"pure $x")
      List(x)
    }

    override def ap[A, B](ff: List[A => B])(fa: List[A]): List[B] = for {
      f <- ff
      a <- fa
    } yield {
      println(s"ap $a")
      f(a)
    }
  }

  def main(args: Array[String]): Unit = {
    val setOfLists: Set[List[Int]] = Set(1, 2, 3, 4, 5).map(List(_))
    val listOfSets: List[Set[Int]] = setOfLists.unorderedTraverse(identity[List[Int]])(commutativeApplicativeList)
    println(setOfLists)
    println(listOfSets)
  }

}
