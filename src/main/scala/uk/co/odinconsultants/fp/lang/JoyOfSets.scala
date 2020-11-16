package uk.co.odinconsultants.fp.lang

object JoyOfSets {

  def main(args: Array[String]): Unit = {
    val vals          = (1 to 10)
    val permutations  = vals.permutations.toList
    val toSets        = permutations.map { xs =>
      val aSet = scala.collection.mutable.Set[Int]()
      xs.foreach { x => aSet.add(x) }
      aSet.toList
    }
    println(s"Number of unique permutations        = ${permutations.toSet.size}")
    println(s"Number of unique results from toSet  = ${toSets.toSet.size}")
  }

}
