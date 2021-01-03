package uk.co.odinconsultants.fp.cats.io

import cats.effect.{ExitCode, IO, IOApp}
import cats.data.Kleisli
import cats.implicits._

object KleisliIO extends IOApp {

  val printAndHalve: Int => IO[Int] = x => IO {
    println(s"Halving $x")
    x / 2
  }

  val printAndTimes10: Int => IO[Int] = x => IO {
    println(s"$x * 10")
    x * 10
  }

  def printOut[T]: T => IO[Unit] = x => IO { println(s"output = $x") }

  override def run(args: List[String]): IO[ExitCode] = {
    val x = Kleisli[IO, Int, Int](printAndHalve)
    val y = Kleisli[IO, Int, Int](printAndTimes10)
    val applied: IO[Int] = (x <+> y).apply(10) // "Halving 10"
    (applied.flatMap(printOut)).as(ExitCode.Success) // "output = 5"
  }
}
