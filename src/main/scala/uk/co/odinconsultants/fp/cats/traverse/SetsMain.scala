package uk.co.odinconsultants.fp.cats.traverse

import cats.{Applicative, CommutativeApplicative}
import cats.implicits._

/**
 * @see https://stackoverflow.com/questions/53945996/cats-effecthow-to-transform-mapx-ioy-to-iomapx-y
 */
object SetsMain {

  val commutativeApplicativeList = new CommutativeApplicative[List] {
    override def pure[A](x: A): List[A] = {
      println(s"pure:  %50s".format(x))
      implicitly[Applicative[List]].pure(x)
    }

    override def ap[A, B](ff: List[A => B])(fa: List[A]): List[B] = {
      val result = implicitly[Applicative[List]].ap(ff)(fa)
      println("ap: fa = %30s          result = %50s".format(fa, result))
      result
    }
  }

  /**
   * Can't use implicits for this one as you'd get something like:
   * Error:(24, 19) Could not find an instance of Applicative for Set
   * = implicitly[Applicative[Set]].ap(ff)(fa)
   **/
  val commutativeApplicativeSet = new CommutativeApplicative[Set] {
    override def pure[A](x: A): Set[A] = Set(x)

    override def ap[A, B](ff: Set[A => B])(fa: Set[A]): Set[B] = for {
      f <- ff
      a <- fa
    } yield f(a)
  }

  def main(args: Array[String]): Unit = {
    val setOfLists: Set[List[Int]] = Set(1, 2, 3, 4, 5).map(List(_))
    val listOfSets: List[Set[Int]] = setOfLists.unorderedTraverse(identity[List[Int]])(commutativeApplicativeList)
    println()
    println(setOfLists)
    println(listOfSets)
  }

}
