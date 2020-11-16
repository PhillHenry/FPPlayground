package uk.co.odinconsultants.fp.lang

import java.util

import scala.collection.mutable

object JoyOfSets {

  type Permutations[T] = Seq[Seq[T]]

  def viaSet[T](xs: Permutations[T]) = xs.map { xs =>
    import scala.collection.JavaConverters._
    val asSet = scala.collection.mutable.Set[T]()
    xs.foreach( x => asSet.add(x) )
    val jList = new util.ArrayList[T](asSet.asJava)
    jList.asScala.toList
  }

  class MyIntWrapper(x: Int) {
    override def toString: String = x.toString
  }

  def main(args: Array[String]): Unit = {
    val vals          = (1 to 7).map(new MyIntWrapper(_))
    val permutations  = vals.permutations.toList
    val toSets        = viaSet(permutations)
    println(s"Number of unique permutations        = ${permutations.toSet.size}")
    println(s"Number of unique results from toSet  = ${toSets.toSet.size}")
    println(s"Sample toSet:\n${toSets.toSet.take(5).mkString("\n")}")
  }

}
