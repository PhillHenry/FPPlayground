package uk.co.odinconsultants.fp.cats.applicatives

import cats.implicits._
import cats.{Functor, Semigroupal}

object MyProduct {

  def myProduct[F[_]: Functor: Semigroupal](x: F[Int], y: F[Int]): F[(Int, Int)] = x.product(y)

}
