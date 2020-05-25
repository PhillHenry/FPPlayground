package uk.co.odinconsultants.fp.zio.streams

import zio.stream.ZStream
import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion._
import zio.test.TestAspect._

object LargePipeMainSpec extends DefaultRunnableSpec {

  import PipeMain._

  val original      = "0123456789" * 10
  val originalBytes = original.map(_.toByte).toList

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("efficiency of large streams")(
      testM ("should maintain integrity"){
        assertM(piping(ZStream.fromIterable(originalBytes), 100).runCollect.map(_.size))(equalTo(originalBytes.size))
      }
    )
  }

}
