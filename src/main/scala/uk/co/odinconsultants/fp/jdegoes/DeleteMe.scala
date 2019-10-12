package uk.co.odinconsultants.fp.jdegoes

object DeleteMe {

  import scala.util.{Success, Try}

  def checkFn[A, B](a: A, fn: A => B): Try[B]  = Try(fn(a))

  def isPossible[A](x: Try[A]): Boolean =
    x match {
      case Success(_) => true
      case _          => false
    }

  def main(args: Array[String]): Unit = {
    val string2Int: String => Int = _.toInt

    println(isPossible(checkFn("5", string2Int))) // true
    println(isPossible(checkFn("a", string2Int))) // false
  }

}
