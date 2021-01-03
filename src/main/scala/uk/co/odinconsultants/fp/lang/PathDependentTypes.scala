package uk.co.odinconsultants.fp.lang

/**
 * https://stackoverflow.com/questions/2693067/what-is-meant-by-scalas-path-dependent-types
 */
object PathDependentTypes {
  case class Board(length: Int, height: Int) {
    case class Coordinate(x: Int, y: Int) {
      require(0 <= x && x < length && 0 <= y && y < height)
    }
    val occupied = scala.collection.mutable.Set[Coordinate]()
  }

  val b1 = Board(20, 20)
  val b2 = Board(30, 30)
  val c1 = b1.Coordinate(15, 15)
  val c2 = b2.Coordinate(25, 25)
  b1.occupied += c1
  b2.occupied += c2

  def main(args: Array[String]): Unit = {
    // Next line doesn't compile
//    b1.occupied += c2
  }
}
