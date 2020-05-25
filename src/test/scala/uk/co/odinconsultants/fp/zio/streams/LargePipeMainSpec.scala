package uk.co.odinconsultants.fp.zio.streams

import zio.blocking.Blocking
import zio.clock.Clock
import zio.stream.ZStream
import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion._
import zio.test.TestAspect._

object LargePipeMainSpec extends DefaultRunnableSpec {

  import PipeMain._

  val n = 100
  val original      = ("0123456789" * n) + "remainder"
  val originalBytes = original.map(_.toByte).toList

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("efficiency of large streams")(
      testM ("should maintain integrity"){
        val actual: ZStream[Clock with Blocking, Throwable, Exchange] = piping(ZStream.fromIterable(originalBytes), n, false)
        assertM(actual.runCollect.map(x => new String(x.map(_.toByte).toArray)))(equalTo(original))
      }
    )
  }

}
