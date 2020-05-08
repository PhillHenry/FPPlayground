package uk.co.odinconsultants.fp.zio.test

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test.junit.JUnitRunnableSpec
import zio.test.{assert, _}

object ZioTestIgnoringAssertionMaybeBug extends JUnitRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = suite("Look closely....")(
    testM("Why is an obvious;y wrong assertion passing") {
      ZIO("Actual").map { x =>
        assert(x)(equalTo("Actual")) &&
          assert(x.toUpperCase)(equalTo("ACTUAL")) &&
          assert(x.toUpperCase)(equalTo("Obviously wrong")) &&
          assert(x.length)(equalTo(6))
          assert(x.toLowerCase)(equalTo("actual"))
      }
    }
  )

}
