package uk.co.odinconsultants.fp.cats.validation

import cats.data.NonEmptyList
import cats.{Applicative, ApplicativeError, Semigroup}

class SemigroupApplicativeError[F[_]: Applicative, X: Semigroup](implicit E: ApplicativeError[F, X]) {

  def allOrNothing[A](xs: NonEmptyList[F[A]]): F[A] = {
    import cats.implicits._
    xs.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

  def aggregate[A: Semigroup](first: F[A], second: F[A], third: F[A]): F[A] = {
    import cats.implicits._
    (first, second, third).mapN { case (x, y, z) =>
        x.combine(y).combine(z)
    }
  }

}
