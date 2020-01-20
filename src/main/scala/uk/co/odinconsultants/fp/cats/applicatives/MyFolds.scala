package uk.co.odinconsultants.fp.cats.applicatives

//import cats.effect.IOApp
import cats.implicits._

object MyFolds {

  def main(args: Array[String]): Unit = {
    List(5.some, 6.some, 7.some).foldMap(identity) // Some(18)
//    List(5.some, 6.some, 7.some).foldMapA(identity) // Some(18)

    List(5.some, 6.some, None, 7.some).foldMap(identity) // Some(18)
//    List(5.some, 6.some, None, 7.some).foldMapA(identity) // None
  }

}
