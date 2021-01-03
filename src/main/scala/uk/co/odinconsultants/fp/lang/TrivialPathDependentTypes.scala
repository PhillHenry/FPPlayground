package uk.co.odinconsultants.fp.lang

/**
 * See https://stackoverflow.com/questions/9338709/what-is-dependent-typing
 */
object TrivialPathDependentTypes {
  case class Integer(v: Int) {
    object IsEven { require(v % 2 == 0) }
    object IsOdd { require(v % 2 != 0) }
  }

  def f(n: Integer)(implicit proof: n.IsEven.type) =  {
    println(s"$n is proved to be even at compile time")
  }

  val `42` = Integer(42)
  implicit val proof42IsEven = `42`.IsEven

  val `1` = Integer(1)
  implicit val proof1IsOdd = `1`.IsOdd

  def main(args: Array[String]): Unit = {
    f(`42`) // OK
//    println(f(`1`))  // compile-time error
  }
}
