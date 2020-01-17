package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}
import cats.implicits._

object MyPull extends IOApp {

  type IntStream = Stream[IO, Int]

  private def rangeStream(n: Int): Stream[Pure, Int] = {
    Stream.range(1, n + 1)
  }

  private def takeAndDrop(n: Int, s: IntStream): IntStream = {
    val pivot = n / 2
    s.take(pivot) ++ s.drop(n - pivot)
  }

  private def effectfulStream(n: Int): IntStream = {
    rangeStream(n).evalMap { x =>
      IO {
        println(s"x = $x")
        x
      }
    }
  }

  def pureStreamOfNums(n: Int): Stream[IO, Int] = {
    val s = rangeStream(n)
    takeAndDrop(n, s)
  }

  def streamOfNums(n: Int): IntStream = {
    val s     = rangeStream(n)
    takeAndDrop(n, s)
  }

  def ioStreamOfNums(n: Int): IntStream = {
    val s = effectfulStream(n)
    takeAndDrop(n, s)
  }

  // Taken from https://fs2.io/guide.html#statefully-transforming-streams
  import fs2._
  def tk[F[_],O](n: Long): Pipe[F,O,O] = {
    def go(s: Stream[F,O], n: Long): Pull[F,O,Unit] = {
      s.pull.uncons.flatMap {
        case Some((hd,tl)) =>
          hd.size match {
            case m if m < n => Pull.output(hd) >> go(tl, n - m)
            case m          => Pull.output(hd.take(n.toInt)) >> Pull.done
          }
        case None => Pull.done
      }
    }
    in => go(in,n).stream
  }
  def drop[F[_],O](n: Long): Pipe[F,O,O] = {
    def go(s: Stream[F,O], n: Long): Pull[F,O,Unit] = {
      s.pull.uncons.flatMap {
        case Some((hd,tl)) =>
          hd.size match {
            case m if m <= n  => go(tl, n - m)
            case m            => Pull.output(hd) >> go(tl, 0)
          }
        case None => Pull.done
      }
    }
    in => go(in,n).stream
  }

  def pullStreamOfNums(n: Int): IntStream = {
    val s = effectfulStream(n)
    val pivot = n / 2
    val head = s.through(tk(pivot))
    head ++ s.through(drop(pivot))
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = pullStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
