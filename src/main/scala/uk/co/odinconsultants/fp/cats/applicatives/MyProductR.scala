package uk.co.odinconsultants.fp.cats.applicatives

import cats.{Applicative, Semigroup}
import cats.data.{NonEmptyList, Validated}

object MyProductR {

  def productR[F[_]: Applicative, A](xs: NonEmptyList[F[A]]): F[A] = {
    println(s"productR $xs")
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

  def aggregateEither[A: Semigroup](xs: NonEmptyList[Either[String, A]]): Either[String, A] = {
    import cats.implicits._
    xs.tail.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

}
