package uk.co.odinconsultants.fp.cats.traverse

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import uk.co.odinconsultants.fp.lang.JoyOfSets
import cats.effect.IO._
import uk.co.odinconsultants.fp.lang.JoyOfSets.MyIntWrapper

object CatSetsMain extends IOApp {

  def traverseFn[T]: T => IO[T]  = {t => IO {
    println(s"t = $t")
    t
  }}

  def run(args: List[String]): IO[ExitCode] = {

    val x: IO[Set[MyIntWrapper]] = JoyOfSets.set1.parUnorderedTraverse { traverseFn }
//    JoyOfSets.set1.parTraverse { x => IO { println(x) } } // No parTraverse on Set
    x.map(_ => ExitCode.Success)
  }
}
