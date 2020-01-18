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

    val doEcho:   IntStream         => Pull[IO, Int, Unit]          = _.pull.echo
    val echoing:  Option[IntStream] => Option[MyPull]               = _.map(doEcho)
    val toPull:   Option[IntStream] => Pull[IO, Int, Unit]          = o => echoing(o).getOrElse(Pull.done)

    val head: IntStream  = s.pull.take(pivot).void.stream

    head ++
//      Pull.output(s.pull.drop(pivot)).void.stream
//      debugStream ++
//      s.pull.drop(pivot).void.stream            // List(1,2,3)
        s.pull.drop(pivot).flatMap(toPull).stream // This is just the code in Stream.drop. Output = List(1,2,3,4,5,6) but also prints out 123123456
//        s.drop(pivot) // List(1,2,3,4,5,6) but also prints out 123123456
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val s = pullStreamOfNums(6)
    s.compile.drain.map { _ => ExitCode.Success }
  }
}
