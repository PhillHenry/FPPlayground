package uk.co.odinconsultants.fp.laziness

object LazyMonad {

  def main(args: Array[String]): Unit = {
    type Contents = Int => Int
    val add1: Contents = _ + 1
    val xs: List[Set[Option[Contents]]] = List(Set(Some(add1)))

    xs.flatMap { x =>
      println("x.flatMap")
      x.flatMap { y =>
        println("y.flatMap" )
        y.map { z =>
          println("z.map")
          z.toString
        }
      }
    }
  }

}
