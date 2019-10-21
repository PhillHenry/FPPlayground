package uk.co.odinconsultants.fp.cats.validation

import cats.data.NonEmptyList
import cats.{Monad, MonadError}

class AsMonad[F[_]: Monad, X](implicit E: MonadError[F, X]) {

  def allOrNothing[A](xs: NonEmptyList[F[A]]): F[A] = {
    import cats.implicits._
    xs.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

}
