package uk.co.odinconsultants.fp

object FibonacciStream {


  val fibs: Stream[Int] = 0 #:: fibs.scanLeft(1)(_ + _) // from the docs: `scanLeft` is analogous to `foldLeft`.

  val f: Stream[Int] = 0  #:: 1  #:: f.zip(f.tail).map { case (x, y) => x + y }

  def main(args: Array[String]): Unit = {
    println("printing...")
    println((f.take(5) ++ f.drop(5)).take(10).mkString(", "))
    println((fibs.take(5) ++ fibs.drop(5)).take(10).mkString(", "))

    val seq: Seq[Int] = fibs // can treat Stream as a Seq
    println(seq.take(5).mkString(", "))
    // but this blows up:
    seq.length
    // to be fair, so does this:
    fibs.length
  }

}
