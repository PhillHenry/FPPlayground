package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}

import zio.ZIO
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test.environment._
import zio.test.{DefaultRunnableSpec, ZSpec, suite, testM, _}

import zio.test.TestAspect._
import zio.console._
import zio.duration._


object PipeMainSpec extends DefaultRunnableSpec {

  import PipeMain._

  val msg: String = "This is a test" //* 100
  val bytes: Array[Byte] = msg.getBytes()

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Pipe")(
    testM("should be non blocking"){
        val inStream  = new ByteArrayInputStream(bytes)
        val outStream = new ByteArrayOutputStream(bufferSize)
        val result: ZIO[Blocking, IOException, TestResult] = for {
          p <- doPipe(inStream, outStream).fork
          _ <- p.join
        } yield {
          assert(new String(outStream.toByteArray))(equalTo(msg))
        }
        result
      }
      ,
      testM("should read slow streams"){
        val managed = for {
          _     <- TestClock.adjust(5.seconds)
          read  <- slowInput.use(x => ZIO(x.read()))
          read2 <- slowInput.use(x => ZIO(x.read()))
        } yield {
          assert(read)(equalTo(0)) //&& assert(read2)(equalTo(1))
        }
        managed
      }
      ,
      testM("should read slow streams"){
        val outStream = new ByteArrayOutputStream(bufferSize)
        val managed = for {
          _   <- TestClock.adjust(5.seconds)
          p <- doBlockingPipe(outStream).use(x => ZIO(x))
        } yield {
          assert(outStream.toByteArray.mkString(","))(equalTo(Array(0,1,2,3,4).map(_.toByte).mkString(",")))
        }
        managed
      } @@ ignore
    )
  }
}
