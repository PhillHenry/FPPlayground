package uk.co.odinconsultants.fp.cats.validation

import cats.{Applicative, ApplicativeError}

class ValidatedAsApplicative[F[_]: Applicative, X](implicit E: ApplicativeError[F, X]) {

}
