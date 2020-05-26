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

      def writing(b: Exchange): Unit = {
        if (logging) println(s"writing $b")
        out.write(b.toArray)
        out.flush()
      }

      val arr = Array.ofDim[Byte](chunkSize)
      def reading(): Exchange = {
        val x = in.read(arr)
        if (logging) println(s"reading ${arr.mkString(",")}")
        Chunk(arr.slice(0, x): _*)
      }

      val s = for {
        byteIn                                                  <- input.chunkN(chunkSize).chunks
        blockingWrite:  ZIO[Blocking, Throwable, Unit]          = effectBlocking(writing(byteIn))
        blockingRead:   ZIO[Blocking, Throwable, Exchange]      = effectBlocking(reading())
        readWrite:      ZStream[Blocking, Throwable, Exchange]  = ZStream.fromEffect(blockingWrite *> blockingRead)
        byteOut                                                 <- readWrite.drainFork(input)
      } yield {
        byteOut
      }

      s
    }

  }
}
