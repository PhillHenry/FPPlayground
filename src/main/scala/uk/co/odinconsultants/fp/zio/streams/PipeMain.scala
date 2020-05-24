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

  val finiteSlowStream: ZStream[Clock, IOException, Byte] =
    infiniteSlowStream.takeUntil(_ == 5)


  def infiniteSlowStream: ZStream[Clock, Nothing, Byte] = {
    val repeatingSleep: ZStream[Clock, Nothing, Unit] = ZStream.repeatEffect(sleep1s)
    repeatingSleep.zipWithIndex.map { x =>
      val b = x._2.toByte
      println(s"Slow stream b = $b")
      b
    }
  }

  val pipes: ZIO[Any, Nothing, (PipedInputStream, PipedOutputStream)] = for {
    out <- ZIO.effectTotal(new PipedOutputStream)
    in  <- ZIO.effectTotal(new PipedInputStream(out))
  } yield (in, out)

  def piping(input: ZStream[Clock, IOException, Byte]): ZStream[Blocking with Clock, Throwable, Int] = {

    ZStream.fromEffect(pipes).flatMap { case (in, out) =>

      def writing(b: Byte): Unit = {
        println(s"writing $b")
        out.write(b)
      }

      def reading(): Int = {
        val x = in.read()
        println(s"reading $x")
        x
      }

      val s = for {
        b <- input
        x <- ZStream.fromEffect(effectBlocking {
            println(s"b = $b")
            out.write(b)
          })
      } yield {
        reading()
      }

      s
    }

  }

  def reading(in: InputStream): ZIO[Blocking, Throwable, Int] = effectBlocking(in.read())

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


