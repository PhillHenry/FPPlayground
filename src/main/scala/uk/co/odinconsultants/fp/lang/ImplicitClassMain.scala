package uk.co.odinconsultants.fp.lang

/**
 * See https://docs.scala-lang.org/overviews/core/implicit-classes.html
 */
object ImplicitClassMain {

  case class X(x: Int)
  case class MyCaseClass(x: String)

  implicit class MyImplicitClass(x: MyCaseClass) { // implicit classes must have exactly one constructor
    def apply(x: Int):  X = X(x)
  }

  def main(args: Array[String]): Unit = {
    val GET     = MyCaseClass("My case class")
    println(GET(42)) // makes it look as if we're constructing a GET but we're not. Just the magic of implict class indirection
  }

}
