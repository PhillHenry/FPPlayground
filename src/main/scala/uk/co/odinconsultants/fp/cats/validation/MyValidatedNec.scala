package uk.co.odinconsultants.fp.cats.validation

import cats.data.{NonEmptyChain, Validated, ValidatedNec}

object MyValidatedNec {

  def aggregate[A](xs: NonEmptyChain[ValidatedNec[Throwable, A]]): Validated[String, NonEmptyChain[A]] = {
    import cats.implicits._
    xs.map(throwableString).sequence
  }

  def concatFn(x: String, y: String): String = s"$x\n$y"

  def throwableString[A](x: ValidatedNec[Throwable, A]): Validated[String, A] = x.leftMap { ts =>
    val errors = ts.map(_.getMessage).foldLeft("") { (acc, x) =>  concatFn(acc, x) }
    errors
  }
}
