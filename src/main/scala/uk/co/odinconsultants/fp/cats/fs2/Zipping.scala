package uk.co.odinconsultants.fp.cats.fs2

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.Signal

import scala.concurrent.duration._

object Zipping extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val nums  = Stream.range[IO](1, 10)
    val chars = Stream.range[IO]('a', 'z' + 1).map(_.toChar)

    println("zipWithHold")
    zipWithHold(nums, chars)

//    println("flatMapOver")
//    flatMapOver(nums, chars)
  }

  private def flatMapOver(nums: Stream[IO, Int], chars: Stream[IO, Char]) = {
    val flatMapped = nums.zip(chars).flatMap { case (a, b) =>
      val x = s"a = $a, b = $b"
      Stream.emit(x)
    }
    printOut(flatMapped)
  }

  private def zipWithHold(x: Stream[IO, Int], y: Stream[IO, Char]): IO[ExitCode] = {
    val nums  = x.metered(25.millis)
    val chars = y.metered(10.millis)
    val zipped = for {
      a <- nums.noneTerminate.hold(1.some)
      b <- chars.noneTerminate.hold('\0'.some)
      c <- combined(a, b)
    } yield c

    printOut(zipped)
  }

  private def combined(a: Signal[IO, Option[Int]], b: Signal[IO, Option[Char]]): Stream[IO, (Int, Char)] =
    (a, b).mapN(_ product _) // product is actually FlatMap.product
      .discrete
      .unNoneTerminate

  private def printOut[T](zipped: Stream[IO, T]): IO[ExitCode] = {
    zipped.evalTap(el => IO {
      println(el)
    }).compile.drain.as(ExitCode.Success)
  }
}