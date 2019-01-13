package uk.co.odinconsultants.fp.scalaz

import scalaz._
import Scalaz._

object FreeMonad extends App {

  case class Free[F[_], A](resume: A \/ F[Free[F, A]])

  type ID[T]                        = T

  type MyType                       = ID[Free[ID, Int]]

  val disjointRight:  MyType \/ Int = 1.right[MyType]
  val disjointLeft:   Int \/ MyType = 1.left[MyType]

  println(disjointLeft)

  val freeLeaf                      = Free(disjointLeft)

  val disjointLeaves: Int \/ MyType = freeLeaf.right[Int]
  val freeBranch                    = Free(disjointLeaves)

  println(freeBranch)
}
