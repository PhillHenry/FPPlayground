package uk.co.odinconsultants.fp.cats.fs2

import cats.effect._
import fs2._

import scala.concurrent.duration._
import cats.implicits._
import fs2.concurrent.Queue

import scala.concurrent.Future

/**
  * @see https://underscore.io/blog/posts/2018/03/20/fs2.html
  *      Note that this uses an old version of the libraries. Migration notes at
 *      https://github.com/functional-streams-for-scala/fs2/blob/series/1.0/docs/migration-guide-1.0.md
  */
object TipsForWorkingWithFS2 extends IOApp {

  type Row = List[String]

  trait CSVHandle {
    def withRows(cb: Either[Throwable,Row] => Unit): Unit
  }

  def rows[F[_]](h: CSVHandle)(implicit F: ConcurrentEffect[F]): Stream[F, Row] =
    for {
      q   <- Stream.eval(fs2.concurrent.Queue.unbounded[F, Either[Throwable,Row]])
      _   <- Stream.eval { F.delay(h.withRows(e => F.runAsync(q.enqueue1(e))(_ => IO.unit))) }
      row <- q.dequeue.rethrow
    } yield row

  val h: CSVHandle = new CSVHandle {
    override def withRows(cb: Either[Throwable, Row] => Unit): Unit = println(s"PH: withRows $cb")
  }

  type F[A] = IO[A]

  override def run(args: List[String]): IO[ExitCode] = {
    printSleepInterleave
  }

  def printSleepInterleave: IO[ExitCode] = {
    val p: F[Unit] = IO { println("My IO") }

    implicit val F = implicitly[ConcurrentEffect[F]]

    val printTime = IO(println(System.currentTimeMillis() % 10000))
    val timesEval: Stream[IO, Unit] = Stream.eval(printTime).repeat
    val sleep: IO[Unit] = Timer[IO].sleep(1.seconds)
    val sleepsEval: Stream[IO, Unit] = Stream.eval(sleep).repeat

    val streamData: Stream[IO, IO[Unit]] = timesEval.zip(sleepsEval).map{ case (a, b) => p}

    streamData.take(5).compile.drain.map(_ => ExitCode.Success)
  }

}
