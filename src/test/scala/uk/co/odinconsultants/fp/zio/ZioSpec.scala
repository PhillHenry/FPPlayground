package uk.co.odinconsultants.fp.zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{Assertion, DefaultRunnableSpec, TestResult, ZSpec, assert, suite, testM}
import zio.test.environment.TestEnvironment

object ZioSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] = suite("Me playing around with ZIO test")(
    testM("1=1 obviously passes") {
      val shouldSatisfy:  Assertion[Int] => TestResult = assert(1)
      val assertion:      Assertion[Any] = equalTo(1)
      ZIO(shouldSatisfy(assertion))
    }
  )
}
