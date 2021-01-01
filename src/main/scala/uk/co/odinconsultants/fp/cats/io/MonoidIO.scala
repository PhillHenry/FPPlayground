package uk.co.odinconsultants.fp.cats.io

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object MonoidIO extends IOApp {

  type IOType = List[Int]

  val _1: IO[IOType] = IO {
    println(1)
    List(1)
  }

  val _2: IO[IOType] = IO {
    println(2)
    List(2)
  }

  val throwsException: IO[IOType] = IO.raiseError(new Throwable("boom!"))

  def combineAndPrint(fa: IO[IOType], fb: IO[IOType]): IO[Unit] = (fa <+> fb).flatMap(x => IO { println(s"x = $x") } )

  override def run(args: List[String]): IO[ExitCode] = {
    combineAndPrint(_1, _2) *>                                  // x = List(1) - doesn't even run _2
      combineAndPrint(throwsException, _2).as(ExitCode.Success) // x = List(2) and runs _2
  }
}
