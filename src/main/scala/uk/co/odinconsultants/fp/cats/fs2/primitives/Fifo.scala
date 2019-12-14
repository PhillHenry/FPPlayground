package uk.co.odinconsultants.fp.cats.fs2.primitives

import cats.implicits._
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import fs2.concurrent.Queue
import fs2.Stream

import scala.concurrent.duration._

/**
 * @see https://fs2.io/concurrency-primitives.html
 */
class Buffering[F[_]](q1: Queue[F, Int], q2: Queue[F, Int])(implicit F: Concurrent[F]) {

  def start: Stream[F, Unit] =
    Stream(
      Stream.range(0, 1000).covary[F].through(q1.enqueue),
      q1.dequeue.through(q2.enqueue),
      //.map won't work here as you're trying to map a pure value with a side effect. Use `evalMap` instead.
      q2.dequeue.evalMap(n => F.delay(println(s"Pulling out $n from Queue #2")))
    ).parJoin(3)
}

object Fifo extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = for {
      q1 <- Stream.eval(Queue.bounded[IO, Int](1))
      q2 <- Stream.eval(Queue.bounded[IO, Int](100))
      bp = new Buffering[IO](q1, q2)
      _  <- Stream.sleep_[IO](5.seconds) concurrently bp.start.drain
    } yield ()
    stream.compile.drain.as(ExitCode.Success)
  }
}
