package uk.co.odinconsultants.fp.zio.streams

import zio.clock.Clock
import zio.{App, Schedule, ZIO}
import zio.stream._
import zio.console._
import zio.duration._

object FromIterableMain extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    /*
    egast05/03/2020
@luis3m if this is the expected behavior it is not very obvious. If I use the following code and print what is
published (and put into the buffer) and what is consumed from the buffer I see the following behavior:
     */
    val zio0 = for {
      result <- Stream.fromIterable(List(1, 2, 2, 3, 4, 5, 6, 6, 6, 7, 8))
        .tap(v => putStrLn(s"produce to buffer: $v"))
        .bufferSliding(1)
        .tap(v => putStrLn(s"pulling from buffer: $v"))
        .schedule(Schedule.fixed(2.second))
        .tap(v => putStrLn(s"result: $v"))
        .runDrain
        .fork
      _ <- result.join.timeout(10.seconds)
    } yield 0

    /*
Itamar Ravid05/03/2020:
@luis3m bufferSliding buffers whole chunks in master currently
 */
    val zio: ZIO[Clock with Console, Nothing, Option[Unit]] = Stream
      .fromIterable(List(1, 2, 2, 3, 4, 5, 6, 6, 6, 7, 8))
      .tap(v => putStrLn(s"produce to buffer: $v"))
      .bufferSliding(1)
      .tap(v => putStrLn(s"pulling from buffer: $v"))
      .schedule(Schedule.fixed(2.second))
      .tap(v => putStrLn(s"result: $v"))
      .runDrain
      .timeout(10.seconds)

    /*
    egast05/03/2020:
    Is see that everything is produced to the buffer before the first pull, but the first pull get the the first published
    element and the second pull gets the (correct) last element. Can you explain this behavior? Do you know how I can
    implement it in such a way that it not pull the first element?
     */
    val zio2 = for {
      result <- Stream.fromIterable(List(1, 2, 2, 3, 4, 5, 6, 6, 6, 7, 8))
        .tap(v => putStrLn(s"produce to buffer: $v"))
        .bufferSliding(1)
        .tap(v => putStrLn(s"pulling from buffer: $v"))
        .schedule(Schedule.fixed(2.second))
        .tap(v => putStrLn(s"result: $v"))
        .runDrain
        .fork
      _ <- result.join.timeout(10.seconds)
    } yield 0


    zio.map(_ => 0)
//    zio.fold(_ => 1, _ => 0)
  }

}
