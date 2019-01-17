package uk.co.odinconsultants.fp.applicative

object MyApplicative extends App {

  def add1(x: Int): Int = x + 1

  def myAp(x: Option[Int => Int]): Option[Int] => Option[Int] = { y =>
    for {
      z <- y
      f <- x
    } yield f(z)
  }

  println(myAp(Some(add1 _))(Some(42)))

}
