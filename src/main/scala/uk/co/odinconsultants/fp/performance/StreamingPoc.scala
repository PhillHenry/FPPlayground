package uk.co.odinconsultants.fp.performance


import java.text.NumberFormat
import java.time.Instant

import cats.effect.{IO, Sync, Timer}
import fs2.{Chunk, Stream}
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable

/**
 * @see https://gist.github.com/thobson/5dc00dc03f7d8656422d62427ee0f5e7
 */
object StreamingPoc {

  import cats.syntax.apply._
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  private implicit val ioTimer: Timer[IO] = IO.timer(global)
  private implicit val scheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  def main(args: Array[String]): Unit = {
    Thread.sleep(5000)
    println("fs2")
    val fs2Op = time(fs2Count())
    val fs2Ops = fs2Op *> IO.sleep(2.seconds) *> fs2Op *> fs2Op
    fs2Ops.unsafeRunSync()

    println("monix")
    val monixOp = time(monixCount())
    val monixOps = monixOp *> Task.sleep(2.seconds) *> monixOp *> monixOp
    monixOps.runSyncUnsafe(1.minute)
  }

  def fs2Count(): IO[Unit] = {
    for {
      // 5M elements in a list
      stream <- IO.delay { Stream.emits(list()).covary[IO] }
      count <- stream.chunkLimit(4096 * 256).compile.fold(0) { (acc, b) => acc + b.size }
      _ <- printCount[IO](count)
    } yield ()
  }

  def fs2CountRevised(): IO[Unit] = {
    for {
      // 5M elements in a list
      stream <- IO.delay { Stream.emits(list()).covary[IO] }
      count <- stream.compile.fold(0) { (acc, _) => acc + 1 }
      _ <- printCount[IO](count)
    } yield ()
  }

  def fs2CountOriginal(): IO[Unit] = {
    for {
      // iterator() gives an interator with 5,000,000 elements
      stream <- IO.delay { Stream.fromIterator[IO](iterator()) }
      count <- stream.compile.fold(0) { (acc, _) => acc + 1 }
      _ <- printCount[IO](count)
    } yield ()
  }

  def monixCountOriginal(): Task[Unit] = {
    // iterator() gives an interator with 5,000,000 elements
    val stream = Observable.fromIterator { Task(iterator()) }
    val count = stream.foldLeftL(0)((acc, _) => acc + 1)
    count.flatMap(c => printCount[Task](c))
  }

  def monixCount(): Task[Unit] = {
    // 5M elements in a list
    val stream = Observable.fromIterable(list())
    val count = stream.foldLeftL(0)((acc, _) => acc + 1)
    count.flatMap(c => printCount[Task](c))
  }

  def iterator(): Iterator[Int] = (1 to 5000000).iterator
  def list(): List[Int] = (1 to 5000000).toList

  def printCount[F[_]](count: Int)(implicit F: Sync[F]): F[Unit] = F.delay {
    val formatted = NumberFormat.getIntegerInstance.format(count)
    println(s"count: $formatted")
  }

  def time[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] = {
    for {
      start <- F.delay(Instant.now())
      a <- fa
      end <- F.delay(Instant.now())
      duration <- F.delay(java.time.Duration.between(start, end))
      _ <- F.delay(println(s"$duration \n"))
    } yield a
  }

}
