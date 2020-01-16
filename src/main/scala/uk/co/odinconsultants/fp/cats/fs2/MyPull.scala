package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import cats.implicits._

object MyPull extends IOApp {

  def streamOfNums(n: Int): Stream[IO, Int] = {
    val s = Stream.range(1, n+1)
    val pivot = n /2
    s.take(pivot) ++ s.drop(n - pivot)
  }

  def effectfulStreamOfNums(n: Int): Stream[IO, Int] = {
    val s = Stream.range(1, n+1).covary[IO]
    val pivot = n /2
    s.take(pivot) ++ s.drop(n - pivot)
  }

  def ioStreamOfNums(n: Int): Stream[IO, Unit] = {
    val s           = Stream.range(1, n+1).evalMap { x => IO { println(s"x = $x") } }
    val pivot       = n /2
    val resumeFrom = n - pivot
    println(s"n = $n, pivot = $pivot, resumeFrom = $resumeFrom")
    val dropped     = s.drop(resumeFrom)
    val taken       = s.take(pivot)
    taken ++ dropped
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = ioStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
