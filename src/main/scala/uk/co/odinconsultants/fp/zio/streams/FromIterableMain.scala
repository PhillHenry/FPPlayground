package uk.co.odinconsultants.fp.zio.streams

import zio.{App, Schedule, ZIO}
import zio.stream._
import zio.console._
import zio.duration._

object FromIterableMain extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val zio = Stream
      .fromIterable(List(1, 2, 2, 3, 4, 5, 6, 6, 6, 7, 8))
      .tap(v => putStrLn(s"produce to buffer: $v"))
      .bufferSliding(1)
      .tap(v => putStrLn(s"pulling from buffer: $v"))
      .schedule(Schedule.fixed(2.second))
      .tap(v => putStrLn(s"result: $v"))
      .runDrain
      .timeout(10.seconds)
    zio.map(_ => 0)
//    zio.fold(_ => 1, _ => 0)
  }

}
