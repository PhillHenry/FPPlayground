package uk.co.odinconsultants.fp.cats.tupled

import cats.{Semigroup, Semigroupal}

object MyTupled {

  import cats.implicits._

  def tupled[A, B](t1: Left[A, B], t2: Left[A, B]): Either[A, (B, B)] = {
    //(t1, t2).tupled
    ???
  }

}
