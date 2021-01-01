package uk.co.odinconsultants.fp.zio.errors

import zio.{Cause, URIO}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, testM, _}
import zio.test.TestAspect._

object ExceptionsInZioSpec extends DefaultRunnableSpec {

  import ExceptionsInZio._

  type Mitigated = Either[Cause[Throwable], String]

  override def spec: ZSpec[TestEnvironment, Any] = suite("error handling")(
    testM("creation OK, acquire OK, release barfs") {
//      val x: ZIO[Any, Throwable, String] = ZIO { throw closeException }
//      val either: URIO[Any, Either[Throwable, String]] = x.either // this works
      val either: URIO[Any, Mitigated] = failRelease.sandbox.either // this fails

      assertM(either.left.map(_.defects.head))(equalTo(closeException))
    },
    testM("handle pathogen") {
      val handled = handlePathogen(failRelease)

      assertM(handled)(equalTo(closeMessage))
    }
  )
}
