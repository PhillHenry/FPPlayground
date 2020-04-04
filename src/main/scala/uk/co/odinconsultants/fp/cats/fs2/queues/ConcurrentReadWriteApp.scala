package uk.co.odinconsultants.fp.cats.fs2.queues

import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream
import fs2.concurrent.InspectableQueue
import cats.implicits._

/**
Hudson Clark @hudclark Apr 01 00:12
I appreciate the help @SystemFw. Sounds like I'll need to use the Concurrent#cancelable builder to explicitly enqueue a None value when the stream is to cancelled. The downside to that is I can't expose a Stream[F, Message] in my API and still have the "graceful" shutdown. Do you know of a way to enqueue a None value and still expose a stream in my public API?

Gavin Bisesi @Daenyth Apr 01 15:06
@hudclark re: having code wait for "clean" shutdown of a NoneTerminatedQueue

It's easiest if you can control producer+consumer in one location because you can do something like:

val read = q.dequeue.flatMap(consume) ++ isFinishedSignalRef.set(true)
val write = producer.run(q) ++ q.enqueue1(None)
Stream(
  (read concurrently write),
  isFinishedSignalRef.discrete.dropWhile(_ == false).take(1) >> runOnTermination
).parJoinUnbounded
 */
object ConcurrentReadWriteApp extends IOApp {

  class PubSubMessageEnvelope[_] { }

  def createPublisher(queue: InspectableQueue[IO, Option[PubSubMessageEnvelope[IO[_]]]]): IO[(PubSubMessageEnvelope[IO[_]], IO[Unit])] = ???

  override def run(args: List[String]): IO[ExitCode] = {
    val prefetch = 10
    Stream.eval(InspectableQueue.bounded[IO, Option[PubSubMessageEnvelope[IO[_]]]](prefetch)).flatMap { queue =>
      val read = queue.dequeue.unNoneTerminate
      // When the publisher closes, it will enqueue a None, stopping the queue
      val publisher = Stream.resource(Resource(createPublisher(queue)))
//      val stopper = publisher *> Stream.eval(stop.get.attempt).take(1) // no idea where this 'stop' comes from
//      read concurrently stopper
      ???
    }

    IO(ExitCode.Error)
  }
}
