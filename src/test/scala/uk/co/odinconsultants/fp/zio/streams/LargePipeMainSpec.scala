package uk.co.odinconsultants.fp.zio.streams

import zio.{ZIO, stream}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.stream.ZStream
import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.duration._

object LargePipeMainSpec extends DefaultRunnableSpec {

  import LargePipeMain._

  val n = 10000
  val original      = ("0123456789" * n) + "remainder"
  val originalBytes = original.map(_.toByte).toList

  def toString(xs: List[Exchange]): String = xs.map(exchangeToString).mkString("")
  def exchangeToString(x: Exchange): String = new String(x.toList.toArray)

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Piping")(
      testM ("piping with Java's PipedInputStream is fine but only for small chunk"){
        val actual = piping(ZStream.fromIterable(originalBytes), 1024, false)
        for {
          sameSize    <- assertM(actual.runCollect.map(x => toString(x).length))(equalTo(original.length))
          sameContent <- assertM(actual.runCollect.map(toString))(equalTo(original))
        } yield sameSize && sameContent

      } @@ timeout(10 seconds)
      ,
      testM ("Large chunks jsut seem to hang"){
        val actual = piping(ZStream.fromIterable(originalBytes), n/2, false)
        for {
          sameSize    <- assertM(actual.runCollect.map(x => toString(x).length))(equalTo(original.length))
          sameContent <- assertM(actual.runCollect.map(toString))(equalTo(original))
        } yield sameSize && sameContent

      } @@ ignore
      ,

      testM ("Whereas piping large chunks with just ZIO mechanism works"){
        val actual: ZIO[Clock with Blocking, Throwable, List[Exchange]] = piping2(ZStream.fromIterable(originalBytes), 1024, true).runCollect
        for {
          sameSize    <- assertM(actual.map(x => toString(x).length))(equalTo(original.length))
          sameContent <- assertM(actual.map(toString))(equalTo(original))
        } yield sameSize && sameContent

      } @@ timeout(10 seconds)

    )
  }

}
