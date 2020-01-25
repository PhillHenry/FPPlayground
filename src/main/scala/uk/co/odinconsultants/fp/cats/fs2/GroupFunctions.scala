package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.IO
import fs2.{Chunk, Stream}
import cats.implicits._

object GroupFunctions {

  type K = Int
  type V = String
  type KV = (K, V)

  def groupingAdjacentBy(s: Stream[IO, KV]): Stream[IO, (K, Chunk[KV])] = s.groupAdjacentBy(_._1)

//  def groupingBy(s: Stream[IO, KV]): Stream[IO, (K, Chunk[KV])] = s.groupWithin(_._1)

}
