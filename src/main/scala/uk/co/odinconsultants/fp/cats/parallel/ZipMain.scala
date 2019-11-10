package uk.co.odinconsultants.fp.cats.parallel

import cats.implicits._
import cats.data._

/**
 * @see https://typelevel.org/cats/typeclasses/parallel.html
 */
object ZipMain {

  def main(args: Array[String]): Unit = {
    println((List(1, 2, 3), List(4, 5, 6)).mapN(_ + _))
    println((List(1, 2, 3), List(4, 5, 6)).parMapN(_ + _))
  }

}
