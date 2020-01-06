package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.IO
import fs2.Stream

/**
 * This deliberately avoids IOApp to see what implicits are needed.
 *
 */
object ZippingWithoutIOApp {

  import scala.concurrent.duration._, cats.effect.{ContextShift, IO, Timer}
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)

  def main(args: Array[String]): Unit = {
    val nums  = Stream.range[IO](1, 10)
    val chars = Stream.range[IO]('a', 'z' + 1).map(_.toChar)
    val x = nums.metered(25.millis)
    val y = chars.metered(10.millis)
  }

  def fromStream_zipRight_documentation(): Unit = {

    val s = Stream.fixedDelay(1000.millis) zipRight Stream.range(0, 5)
    println("unsafeRunSync")
    println(s.compile.toVector.unsafeRunSync) // this waits until all 5 elements have been zipped
    println("Finished")
  }

}
