package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import cats.implicits._

/**
Fabio Labella @SystemFw Apr 06 15:44
@milanvdm Stream is not Foldable, although it has the logically equivalent operation
 */
object FoldingMain extends IOApp{

  def printOut[T](x: T): IO[T] = IO {
    println(x)
    x
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s:        Stream[IO, Int]                 = Stream.range(0, 10, 1).evalMap(printOut)
    val sLists:   Stream[IO, List[Int]]           = Stream.range(0, 10, 1).map(x => List(x))

    val folded:   Stream[IO, List[Int]]           = sLists.foldMonoid.evalMap(printOut)

    folded.compile.drain.as(ExitCode.Success)
  }
}
