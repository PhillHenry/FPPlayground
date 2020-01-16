package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}

object MyPull extends IOApp {

  type IntStream = Stream[IO, Int]

  def pureStreamOfNums(n: Int): Stream[IO, Int] = {
    val s = Stream.range(1, n+1)
    takeAndDrop(n, s)
  }

  private def takeAndDrop(n: Int, s: IntStream): IntStream = {
    val pivot = n / 2
    s.take(pivot) ++ s.drop(n - pivot)
  }

  def streamOfNums(n: Int): IntStream = {
    val s     = Stream.range(1, n + 1)
    takeAndDrop(n, s)
  }

  def ioStreamOfNums(n: Int): IntStream = {
    val s = Stream.range(1, n + 1).evalMap { x =>
      IO {
        println(s"x = $x")
        x
      }
    }
    takeAndDrop(n, s)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = ioStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
