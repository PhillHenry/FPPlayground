package uk.co.odinconsultants.fp.cats.laws

import cats.data.ZipList
import cats.data.ZipList._
import cats.implicits._
import cats.{Applicative, Apply}

/**
 * Seems like ZipList can't have an Applicative as pure can put anything into its container and ZipList only takes a List
 *
 * @see https://typelevel.org/cats/typeclasses/parallel.html#nonemptyparallel---a-weakened-parallel
 */
object ZipListMain /*extends IOApp*/ {

  val f: Int => Int = identity _

  def main(args: Array[String]): Unit = {

    val xs = List(1,2,3,4,5)

    val zs: ZipList[Int] = ZipList(xs)

//    println(ZipList.catsDataCommutativeApplyForZipList.ap(ZipList(f))(zs).value.mkString(", "))
//    zs.ap(zs).

    compare(xs)
//    compare(zs) // no Applicative for ZipList
  }

  private def compare[T[Int]](xs: T[Int])(implicit F: Applicative[T] with Apply[T]) = {
    val l = lhs(xs)
    val r = rhs(xs)
    println(s"l = $l, r = $r, l == r? ${l == r}")
  }

  def lhs[T[Int]](fa: T[Int])(implicit F: Applicative[T]): T[Int] = {
    fa.map(f)
  }

  def rhs[T[Int]](fa: T[Int])(implicit F: Applicative[T] with Apply[T]): T[Int] = {
    F.pure(f).ap(fa) // pure -> Applicative, ap -> Apply
  }
}
