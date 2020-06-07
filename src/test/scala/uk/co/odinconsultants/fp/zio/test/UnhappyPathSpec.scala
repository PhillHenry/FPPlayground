package uk.co.odinconsultants.fp.zio.test

import zio.test._
import zio.test.Assertion._
import zio.{Ref, ZIO}
import zio.test.environment.TestEnvironment
import zio.test.environment.TestSystem.Test
import zio._

/**
 * @see https://stackoverflow.com/questions/58662285/how-to-test-an-exception-case-with-zio-test
 */
object UnhappyPathSpec
  extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = suite("ExampleSpec")(
    testM("Example of testing for expected failure") {
      for {
        result <- ZIO.fail("fail").run
      } yield assert(result)(fails(equalTo("fail")))
    }
      ,
    testM("Example of orDie") {
      for {
        result <- ZIO(???).orDie
      } yield assert(result)(equalTo("hello"))
    }
  )
}

