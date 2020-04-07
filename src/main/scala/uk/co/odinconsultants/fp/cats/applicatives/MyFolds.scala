package uk.co.odinconsultants.fp.cats.applicatives

//import cats.effect.IOApp
import cats.implicits._

object MyFolds {

  def main(args: Array[String]): Unit = {
    foldFoldAFoldM(List(5.some, 6.some, 7.some))        // All are Some(18)

    foldFoldAFoldM(List(5.some, 6.some, None, 7.some))  // foldA and foldM are Nones
  }

  private def foldFoldAFoldM(options: List[Option[Int]]) = {
    println(options.foldMap(identity))
    println(options.foldMapA(identity))
    println(options.foldMapM(identity))
  }
}
