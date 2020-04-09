package uk.co.odinconsultants.fp.cats.kleisli

import cats.data.Kleisli

import scala.util.Try

import cats.implicits._

object MyKleisli {

  def stringToInt(x: String): Option[Int]     = Try { x.toInt }.toOption
  def divide10By(x: Int):     Option[Double]  = Try { 10d / x }.toOption

  def main(args: Array[String]): Unit = {
    val kStringToInt: Kleisli[Option, String, Int]    = Kleisli(stringToInt)
    val kDivision:    Kleisli[Option, Int, Double]    = Kleisli(divide10By)
    val combined:     Kleisli[Option, String, Double] = kStringToInt.andThen(kDivision)

    println(combined.run("5")) // Some(2.0)
  }

}
