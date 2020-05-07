package uk.co.odinconsultants.fp.zio.streams

import zio._
import zio.clock.Clock
import zio.console._
import zio.duration._
import zio.stream._

/**
haemin05/02/2020
Hi, I'm getting a fiber interruption after the end of the stream that produces fibers:
 ghostdogpr05/02/2020
forkDaemon instead of fork? So that it doesn’t get interrupted when the parent finishes
 */
object FibreInterruption extends App {

  type RunResult = Unit

  val doSomethingOverNetwork: URIO[Clock, RunResult] =
    ZIO.sleep(1.second)

  val doSomethingOverNetworkPeriodically: ZStream[Console with Clock, Nothing, Fiber[Nothing, RunResult]] =
    ZStream.repeatEffectWith(putStrLn("run") *> doSomethingOverNetwork.fork, Schedule.fixed(100.millis))

  val accumulateResult: Sink[Nothing, Nothing, Fiber[Nothing, RunResult], Int] =
    Sink.foldLeftM(0)((acc, fiber) => fiber.join.as(acc + 1))

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    doSomethingOverNetworkPeriodically
      .take(10)
//      .bufferUnbounded
      .bufferSliding(128) // doesn't interrupt
      .run(accumulateResult)
      .flatMap(res => putStrLn(res.toString).as(0))
}