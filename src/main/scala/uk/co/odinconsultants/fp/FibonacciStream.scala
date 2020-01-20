package uk.co.odinconsultants.fp

object FibonacciStream {


  val fibs: Stream[Int] = 0 #:: fibs.scanLeft(1)(_ + _)

  val f: Stream[Int] = 0  #:: 1  #:: f.zip(f.tail).map { case (x, y) => x + y }

  def main(args: Array[String]): Unit = {

    val spliced = f.take(5) ++ f.drop(5)

    println("printing...")
    println(spliced.take(10).mkString(", "))
  }

}
