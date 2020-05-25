package uk.co.odinconsultants.fp.zio.streams

import java.io.IOException

import uk.co.odinconsultants.fp.zio.streams.PipeMain.pipes
import zio.{Chunk, ZIO}
import zio.blocking.{Blocking, effectBlocking}
import zio.clock.Clock
import zio.stream.ZStream

object LargePipeMain {

  type Exchange = Chunk[Byte]

  def piping(input: ZStream[Clock, IOException, Byte], chunkSize: Int = 5, logging: Boolean = true): ZStream[Clock with Blocking, Throwable, Exchange] = {

    ZStream.fromEffect(pipes).flatMap { case (in, out) =>

      def writing(b: Chunk[Byte]): Unit = {
        if (logging) println(s"writing $b")
        out.write(b.toArray)
        out.flush()
      }

      def reading(): Exchange = {
        val arr = Array.ofDim[Byte](chunkSize)
        val x = in.read(arr)
        if (logging) println(s"reading ${arr.mkString(",")}")
        Chunk(arr.slice(0, x): _*)
      }

      val s = for {
        byteIn <- input.chunkN(chunkSize).chunks
        blockingWrite:  ZIO[Blocking, Throwable, Unit]          = effectBlocking(writing(byteIn))
        blockingRead:   ZIO[Blocking, Throwable, Exchange]      = effectBlocking(reading())
        writingStream:  ZStream[Blocking, Throwable, Unit]      = ZStream.fromEffect(blockingWrite)
        readingStream:  ZStream[Blocking, Throwable, Exchange]  = ZStream.fromEffect(blockingRead)
        byteOut <- (writingStream *> readingStream).drainFork(input)
      } yield {
        byteOut
      }

      s
    }

  }
}
