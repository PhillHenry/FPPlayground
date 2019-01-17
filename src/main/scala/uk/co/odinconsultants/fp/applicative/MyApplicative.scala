package uk.co.odinconsultants.fp.applicative

object MyApplicative extends App {

  def add1(x: Int): Int = x + 1

  def myAp(x: Option[Int => Int]): Option[Int] => Option[Int] = { y =>
    y.flatMap { z =>
      x.map(_(z))
    }
  }

  println(myAp(Some(add1 _))(Some(42)))

}
