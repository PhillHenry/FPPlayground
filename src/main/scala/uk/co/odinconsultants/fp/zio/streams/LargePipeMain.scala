package uk.co.odinconsultants.fp.zio.streams

import java.io.IOException

import uk.co.odinconsultants.fp.zio.streams.PipeMain.pipes
import zio.{Chunk, ZIO}
import zio.blocking.{Blocking, effectBlocking}
import zio.clock.Clock
import zio.stream.ZStream

/**
Itamar Ravid 31/05/2020 at 9:29 AM
The flatMapping there is a bit off
What this says is, for each chunk from the input stream, for each chunk of bytes read from the stream created from the
effect blockingRead *> blockingWrite that also runs the input stream in the background, yield the bytes
You're running the input stream twice
I think you want to drop the .drainFork(input) from the for comprehension there
Then everything runs synchronously: read a chunk from input, do a blocking write then a blocking read, and yield what
you get out of the blocking read
However that could also be simplified to input.chunkN(chunkSize).chunks.mapM(chunk => blockingWrite(chunk) *> blockingRead)

Itamar RavidToday at 9:42 AM
No, it's probably not functionally correct, because you'll be executing the effects of input twice
a for comprehension on streams means pull an element from one stream, and for each element pulled, pull another element
from another stream, and so forth

Itamar RavidToday at 9:44 AM
now drainFork is usually used when you want to run a stream in the background for its effects
E.g. in the one liner above, you're draining the writer stream in the background so the fromInputStream stream has something to read
but if you're just doing stream.drainFork(input), draining input in the background will just read whatever input yields and discard it
 */
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

      input.chunkN(chunkSize).chunks.flatMap { c =>
        val effect = effectBlocking(writing(c)) *> effectBlocking(reading())
        ZStream.fromEffect(effect)
      }
    }

  }
}
