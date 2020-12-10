package uk.co.odinconsultants.fp.cats.fs2.example

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.syntax.functor._
import fs2.{Pipe, Stream}
import fs2.kafka._

import scala.concurrent.duration._


object SuggestionsFromGitter extends IOApp {

  import Settings._

  def toSleepElement(x: Int): IO[Int] = IO {
    Thread.sleep(x * 200)
    x
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val log:  Int => IO[Unit]     = x => IO { println(s"x = $x") }
    val p:    Pipe[IO, Int, Int]  = track(log, 1.seconds)
    val s:    Stream[IO, Int]     = Stream.range(0, 10, 1).evalMap(toSleepElement)
    s.through(p).compile.drain.as(ExitCode.Success)
  }

  /**
   *
  petern @petern-sc Feb 02 23:02
Is there an equivalent to Monix's bufferTimed in fs2? Something like groupWithin but without a max count. I'm guessing this might be due to the difference between push vs pull streams?

My motivation is to have some sort of status tracking, e.g. every minute log through how many items have been processed.
I'm guessing this could be achieved by setting an arbitrarily high count for groupWithin, but I'm curious if there's a more elegant way maybe.
   */
  def track[F[_]: Concurrent: Timer, A](
                                         log: Int => F[Unit],
                                         freq: FiniteDuration
                                       ): Pipe[F, A, A] = in => {
    import cats.implicits._
    Stream.eval(Ref[F].of(0)).flatMap { count =>
      in.chunks
        .evalTap(c => count.update(_ + c.size))
        .flatMap(Stream.chunk)
        .concurrently {
          val value: F[Int] = count.get
          Stream.repeatEval(value.flatMap(log)).metered(freq)
        }
    }
  }

  def plainKafkaConsumer(): IO[ExitCode] = {
    val stream = consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("test2"))
      .evalTap(consumer => IO(consumer.toString).void)
      .evalMap(x => IO.sleep(3.seconds).as(x)) // sleep a bit to trigger potential race condition with _.stream
      .flatMap(_.stream)
      .map(committable => committable.record.key -> committable.record.value)
      .interruptAfter(10.seconds)

    val consumed =
      stream // wait some time to catch potentially duplicated records
        .compile
        .toVector
        .unsafeRunSync

    println(s"Consumed = $consumed")

    stream.compile.drain.as(ExitCode.Success)
  }


}
