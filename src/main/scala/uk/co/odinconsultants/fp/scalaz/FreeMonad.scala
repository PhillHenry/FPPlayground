package uk.co.odinconsultants.fp.scalaz

import scalaz._
import Scalaz._

object FreeMonad extends App {

  case class Free[F[_], A](resume: A \/ F[Free[F, A]])

  type ID[T] = T

  val scalazEitherRight: List[String] \/ Int = 1.right[List[String]]
//  val scalazEitherLeft: Int \/ List[Free[_, Int]]  = 1.left[List[Free[_, Int]]]
//  println(scalazEitherLeft)

//  val freeLeaf = Free(scalazEitherLeft)
//  val freeList = Free[List, Int](List(freeLeaf))
}
