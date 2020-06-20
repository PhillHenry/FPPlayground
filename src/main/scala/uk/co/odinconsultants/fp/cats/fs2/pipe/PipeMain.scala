package uk.co.odinconsultants.fp.cats.fs2.pipe

import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import fs2.{Chunk, Pipe, Pure, Stream}
import cats.implicits._
import fs2.concurrent.Queue

/**
Fabio Labella @SystemFw 02:28
perhaps replacing producer with
val producer = s.chunks.map(Some(_)).onFinalize(q.enqueue1(None))
which is a bit safer than noneTerminate, should interruption strike at the wrong moment

Soren @srnb_gitlab 02:29
which is a bit safer than noneTerminate, should interruption strike at the wrong moment

Why is this?


Fabio Labella @SystemFw 02:29
noneTerminate is map(Some(_)) ++ Stream.emit(None)
so in case of interruption of s before being passed to pipeNonEmpty2, it might not emit None
which in this case would cause the thing to hang
so this would be fine pipeNonEmpty(s).timeout(5.seconds), but this wouldn't pipeNonEmpty(s.timeout(5.seconds))
 */
object PipeMain extends IOApp {

  def pipeNonEmpty2[F[_]: Concurrent, A, B](
                                             underlying: Pipe[F, A, B]
                                           ): Pipe[F, A, B] = { s =>
    Stream.eval(Queue.synchronousNoneTerminated[F, Chunk[A]]).flatMap { q =>
      val producer = s.chunks.noneTerminate.through(q.enqueue)

      val consumer =
        Stream.eval(q.dequeue1).flatMap {
          case None =>
            Stream.empty
          case Some(c) =>
            (Stream.chunk(c) ++ q.dequeue.flatMap(Stream.chunk))
              .through(underlying)
        }

      consumer.concurrently(producer)
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val printEffect: Int => IO[Unit] = x => IO { println(x) }

    val printEach:  Stream[IO, Int] => Stream[IO, Unit] = { _.flatMap(x => Stream.eval(printEffect(x))) }

    val input:      Stream[IO, Int]   = Stream(1,2,3,4,5,6,7,8)
    val piped = pipeNonEmpty2(printEach)

    val stream = piped(input)

    stream.compile.toList.as(ExitCode.Success)
  }
}
