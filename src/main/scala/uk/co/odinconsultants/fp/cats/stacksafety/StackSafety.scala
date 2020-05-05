package uk.co.odinconsultants.fp.cats.stacksafety

import cats.{Eval, Id}
import cats.data.StateT
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object StackSafety extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    //    this is OK:
    val succeeds: (Int, List[Int]) = (0 until 10000).toList.traverse(x => StateT[Eval, Int, Int]((s: Int) => (s, x).pure[Eval])).run(0).value
    //    this stackoverflows:
//    (0 until 10000).toList.traverse(x => StateT[Id, Int, Int]((s: Int) => (s, x))).run(0)

    IO(ExitCode.Success)
  }
}
