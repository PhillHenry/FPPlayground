package uk.co.odinconsultants.fp.zio.errors

import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.{IO, ZIO}

/**
 * @ghostdogpr 23/6/2020 (Discord)
 * @Igosuki .catchAll if you want to catch the error and return a proper value, orDie if you want to terminate
 *         the fiber instead
 *
 */
object ErrorChannelSpec extends DefaultRunnableSpec {

  val e = new Exception()

  val badBoy = ZIO {
    throw e
  }

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("failed ZIOs") (
      testM("map to Nones") {
        assertM(badBoy.option)(equalTo(None))
      }
      ,
      testM("map to Lefts") {
        assertM(badBoy.either)(equalTo(Left(e)))
      }
      ,
      testM("catchAll") {
        assertM(badBoy.catchAll(x => ZIO(x)))(equalTo(e))
      }
      ,
      testM("fails when given Some error") {
        val failing:  IO[Option[String],  Nothing]          = IO.fail(Some("Error")) // E must be Option[_] or .optional fails on the next line
        val task:     IO[String,          Option[Nothing]]  = failing.optional
        assertM(task.run)(fails(equalTo("Error")))
      }
    )
  }
}
