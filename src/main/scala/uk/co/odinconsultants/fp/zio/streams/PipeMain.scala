package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException, InputStream, OutputStream}

import zio.blocking.{Blocking, effectBlockingInterrupt}
import zio.clock.Clock
import zio.{App, Chunk, Schedule, UIO, ZIO}
import zio.stream._
import zio.console._
import zio.duration._

object PipeMain extends App {

  val bufferSize = 1024
  val chunkSize = 512

  def outZIO(outStream: ByteArrayOutputStream): ZIO[Blocking, IOException, ByteArrayOutputStream] = effectBlockingInterrupt(outStream).refineToOrDie[IOException]

  def input(in: InputStream): ZStream[Blocking, IOException, Chunk[Byte]] = {
    ZStream.fromInputStream(in, chunkSize).chunks
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val inStream  = new ByteArrayInputStream(("This is a test" * 100).getBytes())
    val outStream = new ByteArrayOutputStream(bufferSize)

    val result: ZIO[Blocking, IOException, String] = for {
      outputStream <- outZIO(outStream)
      sink: ZSink[Blocking, IOException, Nothing, Chunk[Byte], Int] = ZSink.fromOutputStream(outputStream)
      _ <- input(inStream)
        .run(sink)
        .ensuring(UIO(outputStream.close()))
//        .forkDaemon
    } yield  {
      new String(outputStream.toByteArray)
    }
    result.fold(_.printStackTrace(), x => println(s"success. Length = ${x.length}")).map(_ => 1)
  }
}


