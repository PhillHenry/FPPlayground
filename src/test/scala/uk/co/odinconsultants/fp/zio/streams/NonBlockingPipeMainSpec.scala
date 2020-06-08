package uk.co.odinconsultants.fp.zio.streams

import uk.co.odinconsultants.fp.zio.streams.PipeMain._
import uk.co.odinconsultants.fp.zio.streams.LargePipeMain._
import zio.test.Assertion.equalTo
import zio.test.TestAspect.timeout
import zio.test.environment.{TestClock, TestEnvironment}
import zio.test.{DefaultRunnableSpec, ZSpec, assert, suite, testM}
import zio.duration._

import zio.test.TestAspect._

object NonBlockingPipeMainSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Pipe")(
      testM("should be non blocking"){
        for {
          _ <- TestClock.adjust(10.seconds)
          p <- pipingJavaIO(infiniteSlowStream, 4, true).take(1).runCollect
        } yield {
          assert(p(0).toArray.mkString(","))(equalTo(Array(0,1,2,3).mkString(",")))
        }
      } @@ timeout(10 seconds)
    )
  }

}
