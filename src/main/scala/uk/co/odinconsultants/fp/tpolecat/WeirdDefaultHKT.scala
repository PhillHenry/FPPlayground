package uk.co.odinconsultants.fp.tpolecat

import cats.{Functor, Id}

/**
  * From a gitter comment by Rob Norris in Scala/Scala 4/6/19
  */
object WeirdDefaultHKT {

  case class AnimalF[F[_]](name: F[String] = "Bob")

  def main(args: Array[String]): Unit = {
    println(AnimalF[Id]() )
  }
}
