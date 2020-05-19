package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import zio.test.{DefaultRunnableSpec, ZSpec, suite, testM}
import zio.blocking.{Blocking, effectBlockingInterrupt}
import zio.clock.Clock
import zio.{App, Chunk, Schedule, UIO, ZIO}
import zio.stream._
import zio.console._
import zio.duration._
import zio.test.environment._
import zio.duration._
import zio.{RIO, Schedule, Queue, UIO, URIO}
import zio.console._
import zio.test._
import zio.test.Assertion._

object PipeMainSpec extends DefaultRunnableSpec {

  import PipeMain._

  val msg: String = "This is a test" //* 100
  val bytes: Array[Byte] = msg.getBytes()

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Pipe")(
    testM("should be non blocking"){
        val inStream  = new ByteArrayInputStream(bytes)
        val outStream = new ByteArrayOutputStream(bufferSize)
        for {
          _ <- doPipe(inStream, outStream)
        } yield {
          assert(new String(outStream.toByteArray))(equalTo(msg))
        }
      }
    )
  }
}
