package uk.co.odinconsultants.fp.cats.applicatives

//import cats.effect.IOApp
import cats.implicits._

object MyFolds {

  def main(args: Array[String]): Unit = {
    foldMapMnA(List(5.some, 6.some, 7.some))        // All are Some(18)
    println()
    foldMapMnA(List(5.some, 6.some, None, 7.some))  // foldA and foldM are for Applicatives and Monads

    // from the docs of foldMapK
    import cats._, cats.implicits._
    val f: Int => Endo[String] = i => (s => s + i)
    val x: Endo[String] = List(1, 2, 3).foldMapK(f)
    val a = x("foo")
    println(a)
  }

  private def foldMapMnA(options: List[Option[Int]]) = {
    println("foldMap(identity)    : " + options.foldMap(identity))
    println("foldMapA(identity)   : " + options.foldMapA(identity))
    println("foldMapM(identity)   : " + options.foldMapM(identity))
    println("foldM accumulating   : " + options.foldM(0){ case (acc, x) => x.map(_ + acc)})
    println("foldA                : " + options.foldA)
  }
}
