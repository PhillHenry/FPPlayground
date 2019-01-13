package uk.co.odinconsultants.fp.scalaz

import scalaz._
import Scalaz._

object FreeMonad extends App {

  case class Free[F[_], A](resume: A \/ F[Free[F, A]])

  type ID[T] = T

  type MyType = List[Free[List, Int]]

  val scalazEitherRight:    MyType \/ Int = 1.right[MyType]
  val scalazEitherLeft:     Int \/ MyType = 1.left[MyType]

  println(scalazEitherLeft)

  val freeLeaf = Free(scalazEitherLeft)

  val scalazEitherRightInt: Int \/ MyType = List(freeLeaf).right[Int]
  val freeBranch = Free(scalazEitherRightInt)

  println(freeBranch)
}
