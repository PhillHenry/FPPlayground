package uk.co.odinconsultants.fp.scalaz

import scalaz._
import Scalaz._

object FreeMonad extends App {

  case class Free[F[_], A](resume: A \/ F[Free[F, A]])

  type MyType                       = List[Free[List, Int]]

  val disjointRight:  MyType \/ Int = 1.right[MyType]
  val disjointLeft:   Int \/ MyType = 1.left[MyType]

  println(disjointLeft)

  val freeLeaf                      = Free(disjointLeft)

  val disjointLeaves: Int \/ MyType = List(freeLeaf).right[Int]
  val freeBranch                    = Free(disjointLeaves)

  println(freeBranch)
}
