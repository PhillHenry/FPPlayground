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
      q   <- Stream.eval(fs2.concurrent.Queue.unbounded[F, Either[Throwable,Row]])
      _   <- Stream.eval { F.delay(h.withRows(e => F.runAsync(q.enqueue1(e))(_ => IO.unit))) }
      row <- q.dequeue.rethrow
    } yield row

  val h: CSVHandle = new CSVHandle {
    override def withRows(cb: Either[Throwable, Row] => Unit): Unit = println(s"PH: withRows $cb")
  }

  type F[A] = IO[A]

  override def run(args: List[String]): IO[ExitCode] = {
//    implicit val contextShift: ContextShift[IO] =
//      IO.contextShift(ExecutionContext.global)
    val p = IO { println("My IO") }
//    type F[A] = EitherT[IO, Throwable, A]

    implicit val F = implicitly[ConcurrentEffect[F]]

    val stream = rows(h)
    val syncIO = F.runCancelable(p) { result =>
      result match {
        case Left(throwable: Throwable) => IO(ExitCode.Error)
        case Right(_) => IO(ExitCode.Success)
      }
    }
    val ioCancelable = syncIO.unsafeRunSync() // type is CancelToken[F] which expands to IO[Unit]
    println(s"a = $ioCancelable")

    IO(ExitCode.Success)
  }

}
