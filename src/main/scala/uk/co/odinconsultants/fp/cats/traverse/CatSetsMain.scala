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

  def intSet = {
    val xs = List(1, 2, 3)
    xs.traverse(traverseFn)
  }

  def run(args: List[String]): IO[ExitCode] = {
    /**
     * You get the compilation error:
     * Error:(27, 41) could not find implicit value for parameter P: cats.Parallel.Aux[cats.effect.IO,F]
     *     JoyOfSets.set1.parUnorderedTraverse {
     * if this line below is not in an IOApp. Apparently, it's to do with needing a ContextShift
     */
    val x: IO[Set[MyIntWrapper]] = JoyOfSets.set1.parUnorderedTraverse { traverseFn }
//    JoyOfSets.set1.parTraverse { x => IO { println(x) } } // No parTraverse on Set
    x.map(_ => ExitCode.Success)
  }
}
