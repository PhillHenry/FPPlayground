package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import cats.implicits._

/**
Fabio Labella @SystemFw Apr 06 15:44
@milanvdm Stream is not Foldable, although it has the logically equivalent operation
 */
object FoldingMain extends IOApp{

  def printOut(x: Int): IO[Int] = IO {
    println(x)
    x
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s:        Stream[IO, Int]                 = Stream.range(0, 10, 1).evalMap(printOut)
    val folded:   Stream[IO, Int]                 = s.foldMonoid.evalMap(printOut)
    val compiled: Stream.CompileOps[IO, IO, Int]  = folded.compile
    val drained:  IO[Unit]                        = compiled.drain
    drained.as(ExitCode.Success)
  }
}
