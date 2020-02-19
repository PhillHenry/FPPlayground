package uk.co.odinconsultants.fp.shapeless.numerical

object AraAdkinsMain extends App  {
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.generic.Equal
  import shapeless.Nat
  import shapeless.nat._

  type Exactly[T] = Refined[Int, Equal[T]]

  case class Matrix[ROWS, COLS](nRows: ROWS, nCols: COLS)

  class Matrix1[Rows <: Nat, Cols <: Nat] {
    def mul[R2 <: Nat, C2 <: Nat](
                                   that: Matrix1[R2, C2]
                                 )(implicit ev1: Cols =:= R2): Matrix1[Rows, C2] = {
      ???
    }
  }
  object Matrix1 {
    def apply[Rows <: Nat, Cols <: Nat]: Matrix1[Rows, Cols] = {
      new Matrix1
    }
  }

  val threeByTwo = Matrix1[_3, _2]
  val fourByFour = Matrix1[_4, _4]
  val twoByThree = Matrix1[_2, _3]

  // Does not compile
  //val invalidMul = threeByTwo.mul(fourByFour)

  val validMul = threeByTwo.mul(twoByThree)
}
