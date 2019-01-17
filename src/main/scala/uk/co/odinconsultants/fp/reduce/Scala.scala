package uk.co.odinconsultants.fp.reduce

object Scala {

  def main(args: Array[String]): Unit = {
    val floats = Seq(1.0f, 0.05f, 0.05f)
    println(floats)
    println(floats.sum) // 1.0999999
    println(floats.reduce(_ + _)) // 1.0999999
    println(floats.reduceLeft(_ + _)) // 1.0999999
    println(floats.reduceRight(_ + _)) // 1.1

    println(Seq.empty[Float].reduce(_ + _)) // Exception in thread "main" java.lang.UnsupportedOperationException: empty.reduceLeft
  }

}
