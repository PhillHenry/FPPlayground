package uk.co.odinconsultants.fp.zio.streams

import java.io.{BufferedOutputStream, ByteArrayOutputStream, OutputStream}

import zio.blocking.Blocking
import zio.stream.ZStream
import zio.test.Assertion.startsWith
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.Assertion._

object PipingSpec extends DefaultRunnableSpec  {

  val msg = "this is a test" * 1000
  val bytes: Array[Byte] = msg.getBytes

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Pipe")(
      testM("should be non blocking"){
        val write: OutputStream => Unit = { out =>
          out.write(bytes)
        }
        val stream: ZStream[Blocking, Throwable, Byte] = Piping.readOutputStream(write)
        for {
          res1 <- stream.runCollect
        } yield assert(new String(res1.toArray))(equalTo(msg))
      }
      ,
      testM("should use promises"){
        val write: OutputStream => Unit = { out =>
          out.write(bytes)
        }
        val stream: ZStream[Blocking, Throwable, Byte] = Piping.fromOutputStream(write)
        for {
          res1 <- stream.runCollect
        } yield assert(new String(res1.toArray))(equalTo(msg))
      }
    )
  }
}
