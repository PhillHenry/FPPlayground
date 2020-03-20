package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.IO
import fs2.{Chunk, Stream}
import cats.implicits._

object GroupFunctions {

  type K = Int
  type V = String
  type KV = (K, V)

  def groupingAdjacentBy(s: Stream[IO, KV]): Stream[IO, (K, Chunk[KV])] = s.groupAdjacentBy(_._1)

  type UniqueGroups[K, V] = Map[K, Set[V]]

  def grouping[K, V](c: Chunk[(K, V)], by: K => K): UniqueGroups[K, V] = {
    val seed: Map[K, Set[V]] = Map.empty[K, Set[V]].withDefaultValue(Set.empty[V])
    c.foldLeft(seed) { case (a, (k, v)) =>
      val newVal: Set[V] = a(k) + v
      a + (k -> newVal)
    }
  }

//  def groupingBy(s: Stream[IO, KV]): Stream[IO, (K, Chunk[KV])] = s.groupWithin(_._1)

}
