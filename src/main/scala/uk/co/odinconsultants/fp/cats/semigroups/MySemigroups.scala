package uk.co.odinconsultants.fp.cats.semigroups

import cats.data.NonEmptyList
import cats.implicits._

object MySemigroups {

  /**
   * @see cats.SemigroupK#combineK
   */
  def main(args: Array[String]): Unit = {
    println(nelNoneSome.reduceLeft(_ <+> _))
    println(nelSomeNone.reduceLeft(_ <+> _))
    println(nelList.reduceLeft(_ <+> _))
    println(nelNoneNone.reduceLeft(_ <+> _))
  }

  val nelList: NonEmptyList[List[String]] =
    NonEmptyList.of(List("test"), List("test2"))

  val nelSomeNone: NonEmptyList[Option[String]] =
    NonEmptyList.of[Option[String]](Some("test"), None)

  val nelNoneSome: NonEmptyList[Option[String]] =
    NonEmptyList.of[Option[String]](None, Some("test"))

  val nelNoneNone: NonEmptyList[Option[String]] =
    NonEmptyList.of[Option[String]](None, None)
}
