package uk.co.odinconsultants.fp.cats.applicatives

import cats.Monad
import cats.{Applicative, Apply}
import cats.implicits._


object ApplicativeVsMonad {

  implicit class MyApplicativeOps[F[A]: Applicative, A](x: F[A]) {

    def myApplicativeExtensionMethod(y: F[A]): F[A] = {
      x *> y
    }

  }

  implicit class MyMonadOps[F[A]: Monad, A](x: F[A]) {

    def myMonadExtensionMethod(y: F[A]): F[A] = {
      x *> y
    }

  }

}
