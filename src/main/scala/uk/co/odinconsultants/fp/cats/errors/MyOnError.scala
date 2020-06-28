package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.effect.IO

object MyOnError extends IOApp {
  val o1: IO[Int] = IO(1)
  val o2: IO[Int] = IO(2)
  val o3: IO[Int] = IO(3)
  val o4: IO[Int] = IO(4)
  val badBoy: IO[Int] = IO(1/0)

  val happyPath = for {
    a <- o1.onError { case x => IO(println(s"Error: $x")) }
    b <- o2
    c <- o3
    d <- o4
  } yield a + b + c + d

  val onErrorUnhappyPath = for {
    a <- o1
    b <- badBoy.onError { case x => IO(println(s"Error: $x")) }
  } yield a + b

  val unhappyPath = for {
    a <- o1
    b <- badBoy
  } yield a + b

  override def run(args: List[String]): IO[ExitCode] = {
    happyPath.as(ExitCode.Success)
  }
}
