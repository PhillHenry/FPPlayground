package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException, InputStream, OutputStream, PipedInputStream, PipedOutputStream}

import zio.blocking.{Blocking, effectBlocking, effectBlockingInterrupt}
import zio.clock.Clock
import zio.{App, Chunk, Promise, Schedule, UIO, URIO, ZIO, ZManaged}
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


  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val inStream  = new ByteArrayInputStream(("This is a test" * 100).getBytes())
    val outStream = new ByteArrayOutputStream(bufferSize)
    doPipe(inStream, outStream).fold(_.printStackTrace(), x => println(s"success. Length = ${outStream.toByteArray.length}")).map(_ => 1)
  }

  val sleep1s: URIO[Clock, Unit] = URIO(println("Sleeping...")) *> ZIO.sleep(1 second)

  val slowStream: ZStream[Clock, IOException, Byte] = {
    val repeatingSleep: ZStream[Clock, Nothing, Unit] = ZStream.repeatEffect(sleep1s)
    repeatingSleep.zipWithIndex.takeUntil(_._2 == 5).map(_._2.toByte)
  }

  val pipes: ZIO[Any, Nothing, (PipedInputStream, PipedOutputStream)] = for {
    out <- ZIO.effectTotal(new PipedOutputStream)
    in  <- ZIO.effectTotal(new PipedInputStream(out))
  } yield (in, out)

  def pipeNonBlocking(input: ZStream[Clock, IOException, Byte]): ZStream[Clock, Throwable, Int] = {

    val x: ZStream[Clock, IOException, InputStream] = for {
      pipe    <- ZStream.fromEffect(pipes)
      inByte  <- input
    } yield {
      pipe._2.write(inByte)
      pipe._1
    }

    val out: ZStream[Clock, Throwable, Int] = x.flatMap { in =>
      ZStream.fromEffect(ZIO(in.read()))
    }

    out
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


