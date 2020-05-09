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
        def elapsedSeconds: F[Int] = c.get
      }
      val process = Stream.fixedRate(1.second).evalMap(_ => c.update(_ + 1))
      Stream.emit(api).concurrently(process)
    }

}

object StopWatchMain extends IOApp {

  import StopWatch._

  override def run(args: List[String]): IO[ExitCode] = {
    myCodeRunCreate
  }

  /**
   * From Stream#resource:
   * "Note that `create` returns a `Stream[F, StopWatch[F]]`, even
   * though there is only one instance being emitted: this is less than ideal,"
   */
  def myCodeRunCreate: IO[ExitCode] = {
    val s: Stream[IO, StopWatch[IO]] = create[IO]

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
//    val first:      IO[Int]   = s.head.compile.lastOrError.flatMap(_.elapsedSeconds)
//    val printFirst: IO[Unit]  = first.map(x => println(s"printFirst = $x") )

    val sleepingStream: Stream[IO, Unit] = Stream.eval(IO.sleep(2.second)).metered(1.seconds)

    val io: IO[Int] = for {
      stopWatch <- s.zip(sleepingStream).map(_._1).compile.toList
      time      <- stopWatch.map(_.elapsedSeconds).last
    } yield {
      println(s"time = $time")
      time
    }

    /*printFirst *>*/ io *> IO(ExitCode.Success)
  }

}
