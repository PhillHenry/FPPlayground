package uk.co.odinconsultants.fp.cats.state

import cats.effect._
import cats.effect.concurrent.Ref
import cats.instances.list._
import cats.syntax.all._

import scala.concurrent.duration._

/**
 * From https://typelevel.org/blog/2018/06/07/shared-state-in-fp.html
 */
object sharedstate extends IOApp {

  type MyState          = List[String]
  type MyStateContainer = Ref[IO, MyState]

  def putStrLn(str: String): IO[Unit] = IO(println(s"$str (${Thread.currentThread().getName})"))

  def update(init: MyState, delta: MyState): MyState = {
    println(s"${Thread.currentThread().getName}: update")
    init ++ delta
  }

  def process1(ref: MyStateContainer): IO[Unit] =
    putStrLn(s"${Thread.currentThread().getName}: Starting process #1") *>
      IO.sleep(3.seconds) *>
      ref.update(xs => update(xs, List("#1"))) *>
      putStrLn(s"${Thread.currentThread().getName}: Done #1")

  def process2(ref: MyStateContainer): IO[Unit] =
    putStrLn(s"${Thread.currentThread().getName}: Starting process #2") *>
      IO.sleep(1.seconds) *>
      ref.update(xs => update(xs, List("#2"))) *>
      putStrLn(s"${Thread.currentThread().getName}: Done #2")

  def process3(ref: MyStateContainer): IO[Unit] =
    putStrLn(s"${Thread.currentThread().getName}: Starting process #3") *>
      IO.sleep(2.seconds) *>
      ref.update(xs => update(xs, List("#3"))) *>
      putStrLn(s"${Thread.currentThread().getName}: Done #3")

  def masterProcess: IO[Unit] = {
    val io: IO[Ref[IO, MyState]] = Ref.of[IO, MyState](List.empty[String])
    io.flatMap { ref: MyStateContainer =>
      val ioa = List(process1(ref), process2(ref), process3(ref)).parSequence.void
      ioa *> ref.get.flatMap(rs => putStrLn(s"${Thread.currentThread().getName}: ${rs.toString}"))
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Main thread is '${Thread.currentThread().getName}''")
    masterProcess.as(ExitCode.Success)
  }

}
