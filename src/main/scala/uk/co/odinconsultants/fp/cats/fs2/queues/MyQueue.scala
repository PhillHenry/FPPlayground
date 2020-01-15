package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pipe, Stream}
import fs2.concurrent.{NoneTerminatedQueue, Queue}

object MyQueue extends IOApp {

  import MyQueueOps._

  override def run(args: List[String]): IO[ExitCode] = {
    val q: IO[NoneTerminatedQueue[IO, Work]] = Queue.noneTerminated[IO, Work]
    Stream.eval(q).flatMap { queue =>
      enqueue(List(Work()), queue) ++ dequeue(queue, printing)
    }.compile.drain.map(_ => ExitCode.Success)
  }

}
