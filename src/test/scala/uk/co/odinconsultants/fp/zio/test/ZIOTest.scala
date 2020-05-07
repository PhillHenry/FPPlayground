package uk.co.odinconsultants.fp.zio.test

import java.util.concurrent.TimeUnit

import zio.{Promise, ZIO}
import zio.test.Assertion.equalTo
import zio.test.{Assertion, DefaultRunnableSpec, TestResult, ZSpec, assert, suite, testM}
import zio.test.environment.{TestClock, TestEnvironment}
import zio.test._
import zio.test.TestAspect._
import zio.duration._
import zio._

object ZIOTest extends DefaultRunnableSpec {
  def spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Simple ZIO test") {
      testM("Test fork + TestClock") {
        for {
          promise <- Promise.make[Nothing, Unit]
          advance = TestClock.setTime(1.second) *> clock.currentTime(TimeUnit.SECONDS)
          //          (r1, r2) <- (advance <* promise.succeed(())) <&> (promise.await *> advance)
          //          assert1  =  assert(r1)(equalTo(1L))
          //          assert2  =  assert(r2)(equalTo(2L)) // Won't pass, r2 is 1 instead of 2
          //        } yield assert1 && assert2
        } yield ???
      }
    }
}
