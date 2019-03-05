package uk.co.odinconsultants.fp.cats.fs2

import cats.data.EitherT
import cats.effect._
import fs2._

import scala.concurrent.ExecutionContext

/**
  * @see https://underscore.io/blog/posts/2018/03/20/fs2.html
  *      Note that this uses an old version of the libraries
  */
object TipsForWorkingWithFS2 extends IOApp {

  type Row = List[String]

  trait CSVHandle {
    def withRows(cb: Either[Throwable,Row] => Unit): Unit
  }

  def rows[F[_]](h: CSVHandle)(implicit F: ConcurrentEffect[F]): Stream[F,Row] =
    for {
      q   <- Stream.eval(fs2.concurrent.Queue.unbounded[F,Either[Throwable,Row]])
      _   <- Stream.eval { F.delay(h.withRows(e => F.runAsync(q.enqueue1(e))(_ => IO.unit))) }
      row <- q.dequeue.rethrow
    } yield row

  override def run(args: List[String]): IO[ExitCode] = {
//    implicit val contextShift: ContextShift[IO] =
//      IO.contextShift(ExecutionContext.global)
    val p = IO { println("My IO") }
    val h = new CSVHandle {
      override def withRows(cb: Either[Throwable, Row] => Unit): Unit = println(cb)
    }

//    type F[A] = EitherT[IO, Throwable, A]
    type F[A] = IO[A]
    implicit val F = implicitly[ConcurrentEffect[F]]

    val stream = rows(h)

    IO(ExitCode.Success)
  }

}
