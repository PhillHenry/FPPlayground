package uk.co.odinconsultants.fp.cats.tupled

import cats.{Functor, Semigroupal}
import cats.implicits._

object MyMapN {

  def myMapN[F[_]: Functor: Semigroupal](x: F[Int], y: F[Int]): F[Int] =
    (x, y).mapN { (i, j) => i + j }

  def mapNAndProduct[F[_]: Functor: Semigroupal](x: F[Option[Int]], y: F[Option[Int]]): F[Option[(Int, Int)]] =
    (x, y).mapN { (i, j) => i product j }

}
