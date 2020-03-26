package uk.co.odinconsultants.fp.zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, ZSpec, assert, suite, testM}
import zio.test.environment.TestEnvironment

object ZioSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] = suite("Me playing around with ZIO test")(
    testM("1=1 obviously passes") {
      ZIO(assert(1)(equalTo(1)))
    }
  )
}
