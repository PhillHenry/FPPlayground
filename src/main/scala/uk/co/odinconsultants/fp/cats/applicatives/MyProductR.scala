package uk.co.odinconsultants.fp.cats.applicatives

import cats.Applicative
import cats.data.{NonEmptyList, Validated}

object MyProductR {

  def productR[F[_]: Applicative, A](xs: NonEmptyList[F[A]]): F[A] = {
    import cats.implicits._
    xs.tail.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

  def aggregate[A](xs: NonEmptyList[Validated[String, A]]): Validated[String, A] = {
    import cats.implicits._
    xs.tail.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

}
