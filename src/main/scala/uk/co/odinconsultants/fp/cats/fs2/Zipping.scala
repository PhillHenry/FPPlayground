package uk.co.odinconsultants.fp.cats.fs2

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

import scala.concurrent.duration._

object Zipping extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val nums  = Stream.range[IO](1, 10).metered(25.millis)
    val chars = Stream.range[IO]('a', 'z' + 1).map(_.toChar).metered(10.millis)

    val zipped = for {
      a <- nums.noneTerminate.hold(1L.some)
      b <- chars.noneTerminate.hold('\0'.some)
      c <- (a, b).mapN(_ product _).discrete.unNoneTerminate // product is actually FlatMap.product
    } yield c

    zipped.evalTap(el => IO { println(el) }).compile.drain.as(ExitCode.Success)
  }
}