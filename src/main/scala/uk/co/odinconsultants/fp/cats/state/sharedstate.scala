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

  def putStrLn(str: String): IO[Unit] = IO(println(str))

  def process1(myState: Ref[IO, List[String]]): IO[Unit] = {
    putStrLn("Starting process #1") *>
      IO.sleep(5.seconds) *>
      myState.update(_ ++ List("#1")) *>
      putStrLn("Done #1")
  }

  def process2(myState: Ref[IO, List[String]]): IO[Unit] = {
    putStrLn("Starting process #2") *>
      IO.sleep(3.seconds) *>
      myState.update(_ ++ List("#2")) *>
      putStrLn("Done #2")
  }

  def process3(myState: Ref[IO, List[String]]): IO[Unit] = {
    putStrLn("Starting process #3") *>
      IO.sleep(10.seconds) *>
      myState.update(_ ++ List("#3")) *>
      putStrLn("Done #3")
  }

  def masterProcess: IO[Unit] =
    Ref.of[IO, List[String]](List.empty[String]).flatMap { myState =>
      val ioa = List(process1(myState), process2(myState), process3(myState)).parSequence.void
      ioa *> myState.get.flatMap(rs => putStrLn(rs.toString))
    }

  override def run(args: List[String]): IO[ExitCode] =
    masterProcess.as(ExitCode.Success)

}
