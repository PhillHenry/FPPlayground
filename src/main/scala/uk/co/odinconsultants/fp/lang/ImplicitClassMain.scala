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
    val myCaseClass     = MyCaseClass("My case class")
//    val myImplicitClass = new MyImplicitClass(myCaseClass)
    implicit val myInt: Int = 42
    println(myCaseClass(42))
  }

}
