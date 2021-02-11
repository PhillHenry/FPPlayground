package uk.co.odinconsultants.fp.lang

case class Limits[T: Numeric](x: T, y: T)

abstract class RandomT[T: Numeric] {
  def randomT(): T
}

object Implicits {

  val rnd = new scala.util.Random

  implicit class RandomInt(limits: Limits[Int]) extends RandomT[Int] {
    def randomT(): Int = {
      import limits._
      val lower = math.min(x, y)
      val upper = math.max(x, y)
      val diff  = upper - lower
      rnd.nextInt(diff - 1) + lower
    }
  }

}

object ImplicitsMain {

  import Implicits._

  def main(args: Array[String]): Unit = {
    println(Limits(1, 10).randomT())
  }

  def assertEvenT[T: Numeric](x: Limits[T])(implicit ev: RandomT[T]) = {
    implicit val gen = implicitly[RandomT[T]]
//    x.randomT() // why does this not compile?
    ???
  }
}
