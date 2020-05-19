package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import zio.test.Assertion._
import zio.test.environment._
import zio.test.{DefaultRunnableSpec, ZSpec, suite, testM, _}

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
          p <- doPipe(inStream, outStream).fork
          _ <- p.join
        } yield {
          assert(new String(outStream.toByteArray))(equalTo(msg))
        }
      }
    )
  }
}
