package uk.co.odinconsultants.fp.zio.errors

import zio.URIO
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, testM, _}
import zio.test.TestAspect._

object ExceptionsInCatchesSpec extends DefaultRunnableSpec {

  import ExceptionsInCatches._

  override def spec: ZSpec[TestEnvironment, Any] = suite("error handling")(
    testM("creation OK, acquire OK, release barfs") {
//      val x: ZIO[Any, Throwable, String] = ZIO { throw closeException }
//      val either: URIO[Any, Either[Throwable, String]] = x.either // this works
      val either: URIO[Any, Either[Throwable, String]] = failRelease.either // this fails

      val expected: Either[Throwable, String] = Left(closeException)
      assertM(either)(equalTo(expected))
    } @@ ignore
  )
}
