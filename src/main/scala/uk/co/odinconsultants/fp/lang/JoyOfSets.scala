package uk.co.odinconsultants.fp.lang

object JoyOfSets {

  type Permutations[T] = Seq[Seq[T]]

  def viaSet[T](xs: Permutations[T]):  Permutations[T] = xs.map { xs =>
    val asSet = scala.collection.mutable.Set[T]()
    xs.foreach( x => asSet.add(x) )
    asSet.toList
  }

  class MyIntWrapper(x: Int) {
    override def toString: String = x.toString
  }

  val set1 = Set(1, 5, 2, 4, 3, 6, 7).map(new MyIntWrapper(_))
  val set2 = Set(1, 5, 2, 4, 6, 3, 7).map(new MyIntWrapper(_))

  def main(args: Array[String]): Unit = {
    val vals          = (1 to 7).map(new MyIntWrapper(_))
    val permutations  = vals.permutations.toList
    val toSets        = viaSet(permutations)
    println(s"Number of unique permutations        = ${permutations.toSet.size}")
    println(s"Number of unique results from toSet  = ${toSets.toSet.size}")
    println(s"Sample toSet:\n${toSets.toSet.take(5).mkString("\n")}")

    val x = set1.toList
    val y = set2.toList
    println(s"x = ${x.mkString("")}")
    println(s"y = ${y.mkString("")}")
    println(x == y)
  }

}
