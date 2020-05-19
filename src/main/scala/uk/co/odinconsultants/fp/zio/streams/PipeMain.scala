package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException, InputStream, OutputStream}

import zio.blocking.{Blocking, effectBlockingInterrupt}
import zio.clock.Clock
import zio.{App, Chunk, Schedule, UIO, ZIO}
import zio.stream._
import zio.console._
import zio.duration._

/**
 * Adapted from:
 * https://github.com/zio/zio-process/blob/9f0b40d728d165cecf13cdd4300ef7fe689bb749/src/main/scala/zio/process/Command.scala#L139-L143
 */
object PipeMain extends App {

  val bufferSize = 1024
  val chunkSize = 512

  def outZIO(outStream: ByteArrayOutputStream): ZIO[Blocking, IOException, ByteArrayOutputStream] =
    effectBlockingInterrupt(outStream).refineToOrDie[IOException]

  def input(in: InputStream): ZStream[Blocking, IOException, Chunk[Byte]] =
    ZStream.fromInputStream(in, chunkSize).chunks

  val inStream  = new ByteArrayInputStream(("This is a test" * 100).getBytes())
  val outStream = new ByteArrayOutputStream(bufferSize)

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

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

  def doPipe(inStream: InputStream, outStream: OutputStream): ZIO[Blocking, IOException, String] = for {
    outputStream <- effectBlockingInterrupt(outStream).refineToOrDie[IOException]
    sink: ZSink[Blocking, IOException, Nothing, Chunk[Byte], Int] = ZSink.fromOutputStream(outputStream)
    _ <- input(inStream)
      .run(sink)
      .ensuring(UIO(outputStream.close()))
  } yield  {
    "done"
  }
}


