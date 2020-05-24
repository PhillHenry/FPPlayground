package uk.co.odinconsultants.fp.zio.streams

import uk.co.odinconsultants.fp.zio.streams.PipeMain._
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
          p <- piping(infiniteSlowStream).take(4).runCollect
        } yield {
          assert(p.mkString(","))(equalTo(Array(0,1,2,3).mkString(",")))
        }
      } @@ timeout(15 seconds)
    )
  }

}
