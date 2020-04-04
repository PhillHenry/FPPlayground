package uk.co.odinconsultants.fp.shapeless.numerical

object ToProductsApp {

  def main(args: Array[String]): Unit = {
    import shapeless._
    import shapeless.syntax.sized._
    import syntax.std.tuple._
    import poly._
    import shapeless._, syntax.singleton._
    import shapeless._
    import syntax.singleton._

    val x = List(1, 2, 3)

    println(x.sized(3).map(_.tupled)) // IntelliJ complains but it does compile
  }

}
