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
    val s     = effectfulStream(n)
    val pivot = n / 2

    val debug: IO[Int] = IO.delay {
      println("spliced here")
      -1
    }
    val debugStream: IntStream = Stream.eval(debug)

    type MyPullT[T]   = Pull[IO, T, Unit]
    type MyPull       = MyPullT[Int]
    type ChunkStream  = (Chunk[Int], IntStream)
    type Unconsed     = Pull[IO, INothing, Option[ChunkStream]]

    val maybeChunkStream: Option[ChunkStream] => Pull[IO, Int, Unit] = _ match {
      case None => Pull.done
      case Some((c, s)) =>
        println(s"c = $c, s = $s")
        val remaining = s ++ debugStream // if (c.isEmpty) Stream.empty else Stream.emit(c.head.get)
        remaining.pull.echo
    }


    def injectIntoStream(s: Stream[IO, Int], n: Int): Stream[IO, Int] = {

      val toPull: Option[ChunkStream] => Pull[IO, Int, Unit] = _ match {
        case None =>
          println("finished")
          Pull.pure(None)
        case Some((c, s)) =>
          println(s"c = $c, s = $s")
          val acc = if (n == 1) debugStream ++ s else s
          Pull.suspend((injectIntoStream(acc, n - 1).consChunk(c)).pull.echo)
      }

      s.pull.uncons.flatMap {
        toPull
      }.void.stream
    }


    //    val head:         IntStream = s.pull.take(pivot).void.stream
//    val pullStream = s.pull.uncons.map(_ => None).flatMap(maybeChunkStream).stream
//    pullStream ++
//      Pull.output(s.pull.drop(pivot)).void.stream
//      debugStream ++
//      s.pull.drop(pivot).void.stream            // List(1,2,3)
//      pullStream.pull.uncons.flatMap(toPull).stream // This is just the code in Stream.drop. Output = List(1,2,3,4,5,6) but also prints out 123123456
//        s.drop(pivot) // List(1,2,3,4,5,6) but also prints out 123123456

//    pullStream

//    s.covary[Pure].take(pivot) ++ s.covary[Pure].drop(pivot)
//    /*s.pull.take(pivot).void.stream ++*/ s.pull.takeRight(pivot).flatMap(c => Stream.emit(c.iterator).flatMap(xs => Stream.emit(xs.map(x => IO(x))))).stream
//    injectAt(s.pull.uncons, pivot).void.stream
    injectIntoStream(s, pivot).filter(_ != -1)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = pullStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
