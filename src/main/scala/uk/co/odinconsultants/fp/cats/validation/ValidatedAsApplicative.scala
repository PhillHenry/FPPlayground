package uk.co.odinconsultants.fp.cats.validation


import scala.util.{Failure, Success, Try}

import cats.data.NonEmptyList
import cats.{Applicative, ApplicativeError}

class ValidatedAsApplicative[F[_]: Applicative, X](implicit E: ApplicativeError[F, X]) {

  def allOrNothing[A](xs: NonEmptyList[F[A]]): F[A] = {
    import cats.implicits._
    xs.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

  def doTry[U](f: => U): F[U] = {
    Try(f) match {
      case Success(x) => pure[U, F](x)
      case Failure(x) =>
        ??? // not sure how to do Throwable => X so we can do a E.raiseError(x: X)
    }
  }

  def pure[U, G[_]: Applicative](u: U): G[U] = {
    import cats.implicits._
    u.pure[G]
  }
}
