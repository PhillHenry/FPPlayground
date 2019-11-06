package uk.co.odinconsultants.fp.cats.io

import cats.NonEmptyParallel
import cats.effect.IO
import cats.implicits._
import cats.effect._

object MyCommutativeReduce extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val io = for {
      calc  <- concurrentReduceOption((1 to 100).toArray)(_ + _)
      _     <- IO { println(s"calculation = $calc") }
    } yield {
      calc
    }

    io.map(_ => ExitCode.Success)
  }

//  Oleg Pyzhcov @oleg-py 14:09
//  @drdozer if you want to use IO for multi-core parallelism in intersecting sets, something like this should work:
  def concurrentReduceOption[A](data: Array[A])(op: (A, A) => A)(implicit cs: ContextShift[IO], nep: NonEmptyParallel[IO]): IO[Option[A]] = {
    def go(i: Int, j: Int): IO[A] = IO.suspend {
      if (i == j) IO.pure(data(i))
      else if (j - i == 1) IO.pure(op(data(i), data(j)))
      else {
        val pivot = i + (j - i) / 2
        (go(i, pivot), go(pivot, j)).parMapN(op)
      }
    }
    if (data.isEmpty) IO.pure(None)
    else go(0, data.length - 1).map(_.some)
  }

}
