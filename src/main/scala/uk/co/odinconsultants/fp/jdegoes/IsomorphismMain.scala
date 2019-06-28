package uk.co.odinconsultants.fp.jdegoes

import scalaz.Maybe._
import scalaz.Maybe
import scala.util.Either


/**
  * See https://twitter.com/jdegoes/status/1143931330033418242
  */
object IsomorphismMain {

  type A      = String
  type B      = Int
  type C      = Array[Char]

  def main(args: Array[String]): Unit = {
    val a       = "a"
    val b       = 1

    val eitherAMaybeB: Either[A,        Maybe[B]] = Left(a)
    val eitherAJustB:  Either[A,        Maybe[B]] = Right(Just(b))
    val eitherMaybeAB: Either[Maybe[A], B]        = Right(b)

    val string2Chars: A         => C = _.toCharArray
    val mb2Chars:     Maybe[B]  => C = { mb => Array(mb.getOrElse(2).toChar) }

    println(doFold(eitherAMaybeB, string2Chars, mb2Chars))
  }

  def doFold[A, B](either: Either[A, B], a2c: A => C, b2c: B => C): C = either.fold(a2c, b2c)

}
