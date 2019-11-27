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

  def putStrLn(str: String): IO[Unit] = IO(println(str))

  def process1(ref: MyStateContainer): IO[Unit] =
    putStrLn("Starting process #1") *>
      IO.sleep(3.seconds) *>
      ref.update(_ ++ List("#1")) *>
      putStrLn("Done #1")

  def process2(ref: MyStateContainer): IO[Unit] =
    putStrLn("Starting process #2") *>
      IO.sleep(1.seconds) *>
      ref.update(_ ++ List("#2")) *>
      putStrLn("Done #2")

  def process3(ref: MyStateContainer): IO[Unit] =
    putStrLn("Starting process #3") *>
      IO.sleep(2.seconds) *>
      ref.update(_ ++ List("#3")) *>
      putStrLn("Done #3")

  def masterProcess: IO[Unit] = {
    val io: IO[Ref[IO, MyState]] = Ref.of[IO, MyState](List.empty[String])
    io.flatMap { ref: MyStateContainer =>
      val ioa = List(process1(ref), process2(ref), process3(ref)).parSequence.void
      ioa *> ref.get.flatMap(rs => putStrLn(rs.toString))
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    masterProcess.as(ExitCode.Success)

}
