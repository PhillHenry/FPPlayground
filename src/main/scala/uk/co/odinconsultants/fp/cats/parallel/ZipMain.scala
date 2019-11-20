package uk.co.odinconsultants.fp.cats.parallel

import cats.implicits._
import cats.data._

/**
 * @see https://typelevel.org/cats/typeclasses/parallel.html
 */
object ZipMain {

  def main(args: Array[String]): Unit = {

    // Parallel.parMap2, requires an implicit NonEmptyParallel, P, on which it calls:
    // P.sequential(P.apply.product(P.parallel(ma), P.parallel(mb)))
    // (see Parallel.parProduct)
    val zipped = (List(1, 2, 3), List(4, 5, 6)).parMapN(_ + _)

    // Semigroupal.map2, requires an implicit Semigroupal and Fs (in this case, Lists) to be Functors
    // creates the elements of the final List via Semigroupal.product
    val cartesian = (List(1, 2, 3), List(4, 5, 6)).mapN(_ + _)

    println(cartesian)  // List(5, 6, 7, 6, 7, 8, 7, 8, 9)
    println(zipped)     // List(5, 7, 9)
  }

}
