package uk.co.odinconsultants.fp.cats.semigroups

import cats.data.OptionT
import cats.implicits._

/**
 * The (perhaps unintuitive) behaviour of combining instances of OptionT is due to:
 *
 *    def combineK[A](x: OptionT[F, A], y: OptionT[F, A]): OptionT[F, A] = x.orElse(y)
 *
 * in OptionTSemigroupK[F[_]]
 */
object OptionTMonoids {

  val some1: Some[Int] = Some(1)
  val some2: Some[Int] = Some(2)
  val listSome1 = List(some1)
  val listSome2 = List(some2)
  val some1T    = OptionT[List, Int](listSome1)
  val some2T    = OptionT[List, Int](listSome2)
  val noneT     = OptionT[List, Int](List(None))

  def main(args: Array[String]): Unit = {
    val combined: OptionT[List, Int] = some1T <+> some2T
    println("some1T    <+> some2T:          " + combined)                   // "OptionT(List(Some(1)))"
    println("(some1T   <+> some2T).value:   " + combined.value)
    println("some2T    <+> some1T:          " + (some2T    <+> some1T))
    println("some1T    <+> noneT:           " + (some1T    <+> noneT))
    println("noneT     <+> some1T:          " + (noneT     <+> some1T))
    println("noneT     <+> some1T:          " + (noneT     <+> noneT))
    println("listSome1 <+> listSome2:       " + (listSome1 <+> listSome2))

    /*
    Error:(23, 20) value <+> is not a member of Some[Int]
    println((some1 <+> some2))
     */
    //    println((some1 <+> some2))
  }

}
