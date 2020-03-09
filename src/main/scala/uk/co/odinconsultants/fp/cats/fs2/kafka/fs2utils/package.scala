package uk.co.odinconsultants.fp.cats.fs2.kafka

import cats.effect.{Concurrent, Timer}
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.duration.FiniteDuration

/**
 * Taken from
 * https://gist.github.com/Daenyth/bcb0b4b8978dc9fa00735f374f5f2d92
 *
 * Fabio Labella @SystemFw Mar 06 16:46
 *  (although do note that the problem there is where to put the bound, general unbounded groupBy is not possible for
 *  streaming contexts). In your specific case, you also have a potential problem with kafka ordering, you have to do a
 *  concurrent join, but that can reorder the offset on your topic, and will result in you committing things you are
 *  not meant to commit
 */
package object fs2utils {

  /**
   * Grouping logic used to split a stream into sub-streams identified by a unique key.
   * Be aware that when working with an unbounded number of keys K, if streams never
   * terminate, there can potentially be unbounded memory usage.
   *
   * The emitted streams terminate when the input terminates or when the stream
   * returned by the pipe itself terminates. Termination is graceful and all input elements are emitted
   *
   * @param selector Function to retrieve grouping key from type A
   * @tparam A Elements in the stream
   * @tparam K A key used to group elements into substreams
   * @return Streams grouped by a unique key identifier `K`.
   */
  def groupByUnbounded[F[_], A, K](selector: A => K)(
    implicit F: Concurrent[F]
  ): Pipe[F, A, (K, Stream[F, A])] = { in =>
    Stream
      .resource(KeyedEnqueue.unbounded[F, K, A])
      .flatMap { ke =>
        in.through(KeyedEnqueue.pipe(ke)(selector))
      }
  }

  /** Like `groupByUnbounded` but back pressures the stream when `maxItems` are inside */
  def groupBy[F[_], A, K](maxItems: Long)(selector: A => K)(
    implicit F: Concurrent[F]
  ): Pipe[F, A, (K, Stream[F, A])] = { in =>
    Stream
      .resource(KeyedEnqueue.itemBounded[F, K, A](maxItems))
      .flatMap { ke =>
        in.through(KeyedEnqueue.pipe(ke)(selector))
      }
  }

  /**
   * Like `groupBy` but each substream is concurrently merged, emitting chunks when the substream has
   * `maxChunkSize` pending or when the substream has waited `maxChunkTimeout` without emitting elements,
   * similar to the standard `groupWithin` combinator
   *
   * @param maxTotalItems Backpressure when this many items are "in flight" concurrently
   * @param maxChunkSize Output chunks satisfy: 0 < emittedChunk.size <= maxChunkSize
   * @param maxChunkTimeout Emit chunks smaller than `maxChunkSize` if `maxChunkTimeout` time has elapsed without
   *                        emitting any chunks for a given key `K` and we have elements that match that selector waiting
   * @param selector Output elements satisfy: (key, chunk) => chunk.forall(a => selector(a) == key)
   */
  def groupWithinBy[F[_], A, K](
                                 maxTotalItems: Long,
                                 maxChunkSize: Int,
                                 maxChunkTimeout: FiniteDuration
                               )(
                                 selector: A => K
                               )(implicit F: Concurrent[F], timer: Timer[F]): Pipe[F, A, (K, Chunk[A])] =
    _.through(groupBy(maxTotalItems)(selector)).map {
      case (key, stream) =>
        stream
          .groupWithin(maxChunkSize, maxChunkTimeout)
          .map(chunk => key -> chunk)
    }.parJoinUnbounded
}