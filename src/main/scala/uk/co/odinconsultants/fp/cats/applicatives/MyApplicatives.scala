package uk.co.odinconsultants.fp.cats.applicatives

import cats.Applicative
import cats.implicits._

class MyApplicatives[T[_]: Applicative](T1: T[String], T2: T[String], T3: T[String]) {

  def process: T[String] = (T1, T2, T3).mapN { case (x, y, z) => List(x, y, z).mkString(" ") }

}
