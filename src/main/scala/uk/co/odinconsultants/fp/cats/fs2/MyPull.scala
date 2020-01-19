package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}

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

  import fs2._
  // see https://fs2.io/guide.html#statefully-transforming-streams
  def pullStreamOfNums(n: Int): IntStream = {
    val s         = effectfulStream(n)
    val pivot     = n / 2
    val debugVal  = -1

    val debug: IO[Int] = IO.delay {
      println("spliced here")
      debugVal
    }
    val debugStream: IntStream = Stream.eval(debug)

    type ChunkStream  = (Chunk[Int], IntStream)

    def injectIntoStream(s: Stream[IO, Int], n: Int): Stream[IO, Int] = {

      val toPull: Option[ChunkStream] => Pull[IO, Int, Unit] = _ match {
        case None =>
          Pull.pure(None)
        case Some((c, s)) =>
          val acc = if (n == 1) debugStream ++ s else s
          Pull.suspend((injectIntoStream(acc, n - 1).consChunk(c)).pull.echo)
      }

      s.pull.uncons.flatMap {
        toPull
      }.void.stream
    }

    injectIntoStream(s, pivot).filter(_ != debugVal)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = pullStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
