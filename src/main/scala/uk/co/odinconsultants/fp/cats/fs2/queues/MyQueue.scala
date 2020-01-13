package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pipe, Stream}
import fs2.concurrent.{NoneTerminatedQueue, Queue}

object MyQueue extends IOApp {

  case class Work()

  override def run(args: List[String]): IO[ExitCode] = {
    val workToDo: IO[List[Work]] = IO { List(Work()) }

    val q: IO[NoneTerminatedQueue[IO, Work]] = Queue.noneTerminated[IO, Work]
    Stream.eval(q).flatMap { queue =>
      val enqueing: Pipe[IO, Option[Work], Unit] = queue.enqueue
      val prepare = Stream.eval(workToDo).flatMap(Stream.emits).map(Option(_)).through(enqueing)
      prepare ++ queue.dequeue.evalMap { work =>
//        worker.do(work) >> someBusinessStuff.flatTap(maybeTerminateQueue(_))
        IO { println(s"work = $work") }
      }
    }.compile.drain.map(_ => ExitCode.Success)
  }

}
