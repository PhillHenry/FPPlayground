package uk.co.odinconsultants.fp.cats.fs2.triggers

import cats.effect.{ExitCode, IO, IOApp}

import fs2.Stream
import scala.concurrent.duration._
import cats.implicits._

object TriggerEveryMain extends IOApp {
  val delayStream = Stream
    .iterate[IO, Int](1)(_ + 1)
    .metered(3.seconds)

  override def run(args: List[String]): IO[ExitCode] = {
    val waker: Stream[IO, FiniteDuration] = Stream.awakeEvery[IO](1 second).map { x =>
      println(s"Wake up! It's been ${x._1} now")
      x
    }
    val triggered: Stream[IO, FiniteDuration] = delayStream.switchMap(_ => waker)
    val loggedTrigger = triggered.flatMap { x =>
      val s: Stream[IO, Unit] = Stream.eval(IO { println(s"x = $x") } )
      s
    }
    loggedTrigger.take(3).compile.toList *> IO(ExitCode.Success)
  }
}
