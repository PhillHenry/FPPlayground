package uk.co.odinconsultants.fp.lang

object JoyOfSets {

  type Permutations[T] = List[List[T]]

  def viaSet[T](xs: Permutations[T]): Permutations[T] = xs.map { xs =>
    val aSet = xs.toSet
    aSet.toList
  }

  def main(args: Array[String]): Unit = {
    val vals          = (1 to 6)
    val permutations  = vals.permutations.toList
    val toSets        = permutations
    println(s"Number of unique permutations        = ${permutations.toSet.size}")
    println(s"Number of unique results from toSet  = ${toSets.toSet.size}")
    println(s"Sample toSet:\n${toSets.toSet.take(5).mkString("\n")}")
  }

}
