package uk.co.odinconsultants.fp.cats.fs2.transforming

import cats.effect.IO
import fs2.{Chunk, Pull, Stream}

object Splice {

  /**
   * Not stack safe!
   */
  def intoStream[T](s: Stream[IO, T], n: Int, debugStream: Stream[IO, T]): Stream[IO, T] = {

    val toPull: Option[(Chunk[T], Stream[IO, T])] => Pull[IO, T, Unit] = _ match {
      case None =>
        Pull.pure(None)
      case Some((c, s)) =>
        val acc = if (n == 1) debugStream ++ s else s
        val x = intoStream(acc, n - 1, debugStream).consChunk(c).pull
        x.echo
    }

    s.pull.uncons.flatMap {
      toPull
    }.void.stream
  }

}
