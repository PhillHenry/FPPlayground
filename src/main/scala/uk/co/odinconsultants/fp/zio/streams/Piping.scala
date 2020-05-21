package uk.co.odinconsultants.fp.zio.streams

import java.io.{IOException, OutputStream, PipedInputStream, PipedOutputStream}

import zio.{Exit, Promise, ZIO}
import zio.blocking.Blocking
import zio.stream.{ZStream, ZStreamChunk}
import zio.blocking._

/**
 * From Georgi Krastev on Discord (20/5/20)
 */
object Piping {

  private val pipes: ZIO[Any, Nothing, (PipedInputStream, PipedOutputStream, Promise[Throwable, None.type])] = for {
    out   <-  ZIO.effectTotal(new PipedOutputStream)
    in    <-  ZIO.effectTotal(new PipedInputStream(out))
    error <-  Promise.make[Throwable, None.type]
  } yield (in, out, error)

  def fromOutputStream(
                        write:      OutputStream => Unit,
                        chunkSize:  Int = 4096 //ZStream.DefaultChunkSize
                      ): ZStream[Blocking, Throwable, Byte] =
    ZStream.fromEffect(pipes).flatMap { case (in, out, error) =>
      val readIn: ZStreamChunk[Any, IOException, Byte] = ZStream
        .fromInputStream(in, chunkSize)
        .ensuring(ZIO.effectTotal(in.close()))

      val writeOut = ZStream.fromEffect {
        effectBlocking(write(out))
          .run.tap(exit => error.done(exit.as(None)))
          .ensuring(ZIO.effectTotal(out.close()))
      }

      val handleError = ZStream.fromEffectOption(error.await.some)
      readIn.flattenChunks.drainFork(writeOut) ++ handleError
    }

  /**
   * Itamar RavidYesterday at 1:43 PM
What's erroring? out.close?

  Georgi KrastevYesterday at 1:43 PM
Yeah maybe it happens further down the line, but I print the cause right after

  Itamar RavidYesterday at 1:44 PM
Only out.close could get converted to a Die

  Georgi KrastevYesterday at 2:43 PM
Oh now I see I think: interruptWhen races the completion of the stream with awaiting termination, but completion of the stream is done by the finalizer that closes the output stream  even when write throws an exception.
So I guess I can't use ensuring
Ugh this is so tricky
   */
  def readOutputStream(write: OutputStream => Unit): ZStream[Blocking, Throwable, Byte] = {
    val pipes: ZIO[Any, Nothing, (PipedInputStream, PipedOutputStream)] = for {
      out <- ZIO.effectTotal(new PipedOutputStream)
      in  <- ZIO.effectTotal(new PipedInputStream(out))
    } yield (in, out)

    ZStream.fromEffect(pipes).flatMap { case (in, out) =>
      val chunks: ZStreamChunk[Any, IOException, Byte] = ZStream.fromInputStream(in).ensuring(ZIO.effectTotal(in.close()))
      chunks.flattenChunks.drainFork {
        ZStream.fromEffect(effectBlocking(write(out)).ensuring(ZIO.effectTotal(out.close())))
      }
    }
  }
}
