package uk.co.odinconsultants.fp.cats.fs2.example.docs

import fs2._
import cats.effect._
import cats.effect.concurrent.Ref

import scala.concurrent.duration._
import cats.implicits._

import scala.collection.immutable

trait StopWatch[F[_]] {
  def elapsedSeconds: F[Int]
}

object StopWatch {

  def create[F[_] : Concurrent : Timer]: Stream[F, StopWatch[F]] =
    Stream.eval(Ref[F].of(0)).flatMap { c =>
      val api = new StopWatch[F] {
        def elapsedSeconds: F[Int] = {
          println(s"elapsedSeconds")
          val got: F[Int] = c.get
          got.map { x =>
            println(s"get $x")
            x
          }
        }
      }
      val process = Stream.fixedRate(1.second).evalMap(_ => c.update(_ + 1))
      Stream.emit(api).concurrently(process)
    }

}

object StopWatchMain extends IOApp {

  import StopWatch._

  override def run(args: List[String]): IO[ExitCode] = {
    /*
    Fabio Labella @SystemFw May 11 15:03
    Nothing will run until you compile.
     */
    val s = create[IO]
    aboutToSleepYieldThenNothing(s) *>
      aboutToSleepYieldThenNothing(s) *>
      IO { println("\n\n")} *>
      printsTime1(s) *>
      printsTime1(s) *>
      IO { println("\n\n")} *>
      useResource(s) *>
      useResource(s)
  }

  def printsTime1(s: Stream[IO, StopWatch[IO]]): IO[ExitCode] = {
    val io: IO[Int] = for {
      stopWatch <- s.zip(sleepingStream).map(_._1).compile.toList
      xs        = stopWatch.map(_.elapsedSeconds)
      time      <- xs.last
    } yield {
      println(s"time = $time |xs| = ${xs.length}")
      time
    }

    io *> IO(ExitCode.Success)
  }

  def useResource(s: Stream[IO, StopWatch[IO]]): IO[ExitCode] = {
    val io: Resource[IO, List[IO[Int]]] = for {
      stopWatch <- s.zip(sleepingStream).map(_._1.elapsedSeconds).compile.resource.toList
      xs        = stopWatch
    } yield {
      xs.map{ io =>
        val printIO = io.map { time =>
          println(s"time = $time |xs| = ${xs.length}")
          time
        }
        printIO
      }
    }

    val y: IO[List[Int]] = io.use(_.sequence).map { x =>
      println(s"x = $x")
      x
    }

    y *> y *> IO(ExitCode.Success)
  }

  /**
   * From Stream#resource:
   * "Note that `create` returns a `Stream[F, StopWatch[F]]`, even
   * though there is only one instance being emitted: this is less than ideal,"
   */
  def aboutToSleepYieldThenNothing(s: Stream[IO, StopWatch[IO]]): IO[ExitCode] = {
    /**
     *  "so we might think about returning an `F[StopWatch[F]]` with the following code
     *
     * {{{
     * StopWatch.create[F].compile.lastOrError
     * }}}
     *
     * "but it does not work: the returned `F` terminates the lifetime of the stream,
     * which causes `concurrently` to stop the `process` stream. As a  result, `elapsedSeconds`
     * never gets updated."
     */
    sleepyStopWatch(s).take(5).compile.drain.map(_ => ExitCode.Success) // "yield" but no "flatMap". "About to sleep" but no "sleep over"
  }

  def sleepyStopWatch(s: Stream[IO, StopWatch[IO]]): Stream[IO, IO[Int]] = for {
    stopWatch <- s.zip(sleepingStream).map(_._1)
  } yield {
    println("yield")
    stopWatch.elapsedSeconds.flatMap { t =>
      println("flatMap")
      IO {
        println(s"t = $t")
        t
      }
    }
  }

  val sleepingStream: Stream[IO, Unit] = (Stream.eval(IO { println("About to sleep") } )
    ++ Stream.eval(IO.sleep(20.second)) // this doesn't appear to be executed...
    ++ Stream.eval(IO { println("sleep over") } )
    ).metered(1.seconds)
}
