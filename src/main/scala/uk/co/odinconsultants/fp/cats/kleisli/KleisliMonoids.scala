package uk.co.odinconsultants.fp.cats.kleisli

import cats.data.{Kleisli, OptionT}
import cats.implicits._

object KleisliMonoids {

  val add1      = Kleisli[List, Int, Int](x => List(x + 1))
  val divide10  = Kleisli[List, Int, Int](x => List(x / 10))

  val add1Div10 = add1 <+> divide10

  def main(args: Array[String]): Unit = {
    println("chained: " + add1Div10.run(99))
  }
}
