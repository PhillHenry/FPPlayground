package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.{Concurrent, IO}
import fs2.concurrent.{Dequeue, Enqueue, NoneTerminatedQueue, Queue}
import fs2.{Pipe, Stream}

object MyQueueOps {

  type MyWorkQueue = NoneTerminatedQueue[IO, Work]
  type MyEnqueue   = Option[Work]

  case class Work()

  def initializeQueueWith(xs: List[Work], q: IO[Enqueue[IO, MyEnqueue]])(implicit c: Concurrent[IO]): Stream[IO, Unit] = {
    Stream.eval(q).flatMap { queue =>
      enqueue(xs, queue)
    }
  }

  def enqueue(xs: List[Work], q: Enqueue[IO, MyEnqueue]): Stream[IO, Unit] = {
    val enqueing: Pipe[IO, MyEnqueue, Unit] = q.enqueue
    val sIOs:     Stream[IO, List[Work]]    = Stream.eval(IO(xs))
    val s:        Stream[IO, Work]          = sIOs.flatMap(Stream.emits)
    s.map(Option(_)).through(enqueing)
  }

  type Handling[T] = Any => IO[T]
  val printing: Handling[Unit] = x => IO { println(s"$x") }

  def dequeue[T](queue: Dequeue[IO, Work], io: Handling[T]): Stream[IO, T] = {
    queue.dequeue.evalMap { work =>
      io(work)
    }
  }
}
