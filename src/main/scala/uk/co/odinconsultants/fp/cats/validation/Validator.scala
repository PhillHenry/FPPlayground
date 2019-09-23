package uk.co.odinconsultants.fp.cats.validation

import cats._, cats.data._, cats.implicits._
import cats._
import cats.data._
import cats.implicits._

class Validator[T[_]: Monad, W](TW: T[W], UW: T[W], VW: T[W]) {

  def getT: T[W] = TW
  def getU: T[W] = UW
  def getV: T[W] = VW

  def process: T[String] = for {
    a <- getT
    b <- getU
    c <- getV
  } yield List(a, b, c).mkString(" ")

}

object Validator {

  def main(args: Array[String]): Unit = {
    val TW = Option("Hello")
    val UW = Option("World")
    val VW = Option("(the end)")
    val x = new Validator(TW, UW, VW)
    println(x.process)
  }

}
