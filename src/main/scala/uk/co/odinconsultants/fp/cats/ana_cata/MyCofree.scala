package uk.co.odinconsultants.fp.cats.ana_cata

import cats.free.Cofree
import cats.implicits._

object MyCofree {

  val unfoldedHundred: Cofree[Option, Int] = Cofree.unfold[Option, Int](0)(i => if (i == 100) None else Some(i + 1))

  def main(args: Array[String]): Unit = {
    println(unfoldedHundred.toList)
  }

}
