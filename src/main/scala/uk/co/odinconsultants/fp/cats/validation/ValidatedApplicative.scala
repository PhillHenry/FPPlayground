package uk.co.odinconsultants.fp.cats.validation

import cats.Applicative

class ValidatedApplicative[F[_]: Applicative, X] {

  def pure[U, G[_]: Applicative](u: U): G[U] = {
    import cats.implicits._
    u.pure[G]
  }

}
