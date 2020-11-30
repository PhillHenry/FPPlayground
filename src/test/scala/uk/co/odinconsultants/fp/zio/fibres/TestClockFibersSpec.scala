package uk.co.odinconsultants.fp.zio.fibres

import zio.{Promise, ZIO}
import zio.test._
import zio.test.environment.TestClock
import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, testM, _}
import zio.{Cause, IO, ZIO}
import zio.duration._

object TestClockFibersSpec extends DefaultRunnableSpec {
  /**
   * see https://zio.dev/docs/howto/howto_test_effects#testing-clock
   * mojo11/28/2020
   * Hi, In the zio docs (https://zio.dev/docs/howto/howto_test_effects#testing-clock) there is an example:
   * Isn't it possible for TestClock.adjust(10.seconds) to occur before ZIO.sleep(10.seconds), thus getting stuck
   * forever or is there some kind of magic going on here that I don't understand?
   * Test effects · ZIO
   *
   * luis3m11/28/2020
   * @mojo no, ZIO.sleep gets stuck until the clock is manually advanced which is what TestClock.adjust does
   * mojo replied to luis3m11/28/2020
   * But if it adjusts before the sleep?
   * luis3m11/28/2020
   * @mojo I was wrong it does get stuck if you adjust first, anyway I think it's not supposed to adjust first because
   *      a fiber is a running computation. It should sleep first always in that particular example
   */
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Does/should this deadlock") (
    testM("One can control time as he see fit") {
      for {
        promise <- Promise.make[Unit, Int]
        _       <- (ZIO.sleep(10.seconds) *> promise.succeed(1)).fork
        _       <- TestClock.adjust(10.seconds)
        readRef <- promise.await
      } yield assert(1)(equalTo(readRef))
    }
  )
}
