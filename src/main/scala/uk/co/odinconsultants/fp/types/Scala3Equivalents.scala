package uk.co.odinconsultants.fp.types

object Scala3Equivalents {

  trait Shape extends Product with Serializable

  trait SNil extends Shape

//  type Dimension = Int & Singleton // union types not allowed in Scala 2
  type Dimension = Int

  final case class #:[+H <: Dimension, +T <: Shape](head: H, tail: T) extends Shape {
    // Need Singleton for the first match to compile
//    override def toString = head match {
//      case _ #: _ => s"($head) #: $tail"
//      case _      => s"$head #: $tail"
//    }
  }

  // just totally confuses a Scala 2 compiler
//  type NumElements[X <: Shape] <: Int = X match {
//    case SNil         => 1
//    case head #: tail => head * NumElements[tail]
//  }

  def main(args: Array[String]): Unit = {
    println("Dumping ground for Scala 3 code just to see what works in Scala 2")
  }
}
