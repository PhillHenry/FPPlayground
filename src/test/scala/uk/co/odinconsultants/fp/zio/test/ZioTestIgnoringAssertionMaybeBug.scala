package uk.co.odinconsultants.fp.zio.test

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test.junit.JUnitRunnableSpec
import zio.test.{assert, _}

object ZioTestIgnoringAssertionMaybeBug extends JUnitRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = suite("Possible ZIO test bug?")(
    testM("Something like this ignore assertions in a real test. ") {
      ZIO.fromEither(Right("actual")).map { x =>
        assert(x)(equalTo("actual")) // && assert(x)(equalTo("not actual"))
      }
    }
  )

}
