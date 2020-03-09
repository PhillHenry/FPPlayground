package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.Monad
import cats.effect.concurrent.{Ref, Semaphore}
import cats.effect.{Concurrent, Resource}
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.concurrent.{NoneTerminatedQueue, Queue}

/** Represents the ability to enqueue keyed items into a stream of queues that emits homogenous keyed streams.
 *
 * This allows construction of a "keyed fan-out" behavior for a stream, which may be used for
 * homogenously batching items that arrive via a heterogenous input
 *
 * Somewhat analogous to [[fs2.concurrent.Enqueue]]
 */
private[fs2utils] trait KeyedEnqueue[F[_], K, A] {

  /** Enqueue a single item for a given key, possibly creating and returning a new substream for that key
   *
   * @return <ul><li> None if the item was published to an already-live substream </li>
   *         <li>Some if a new queue was created for this element. This can happen multiple times for the same
   *         key (for example, if the implementation automatically terminates old/quiet substreams).</li></ul>
   *         The returned stream may eventually terminate, but it won't be cancelled by this.
   */
  def enqueue1(key: K, item: A): F[Option[(K, Stream[F, A])]]

  /** Gracefully terminate all sub-streams we have emitted so far */
  def shutdownAll: F[Unit]
}

private[fs2utils] object KeyedEnqueue {

  def unbounded[F[_]: Concurrent, K, A]: Resource[F, KeyedEnqueue[F, K, A]] =
    Resource.liftF(Ref[F].of(Map.empty[K, NoneTerminatedQueue[F, A]])).flatMap {
      st =>
        Resource.make(
          (new UnboundedKeyedEnqueue[F, K, A](st): KeyedEnqueue[F, K, A])
            .pure[F])(_.shutdownAll)

    }

  def pipe[F[_]: Concurrent, K, A](
                                    ke: KeyedEnqueue[F, K, A]
                                  )(selector: A => K): Pipe[F, A, (K, Stream[F, A])] = { in =>
    // Note this *must* be `++` specifically to allow for "input termination = output termination" behavior.
    // Using `onFinalize` will allow the finalizer to be rescoped to the output of this stream later, which
    // results in it not triggering because it's waiting for itself to terminate before it terminates itself
    in.evalMap(a => ke.enqueue1(selector(a), a)).unNone ++
      Stream.eval_(ke.shutdownAll)
  }

  def itemBounded[F[_]: Concurrent, K, A](
                                           maxItems: Long
                                         ): Resource[F, KeyedEnqueue[F, K, A]] =
    for {
      ke <- unbounded[F, K, A]
      limit <- Resource.liftF(Semaphore[F](maxItems))
    } yield new ItemBoundedKeyedEnqueue(ke, limit)

}

private class UnboundedKeyedEnqueue[F[_], K, A](
                                                 queues: Ref[F, Map[K, NoneTerminatedQueue[F, A]]]
                                               )(implicit F: Concurrent[F])
  extends KeyedEnqueue[F, K, A] {
  override def enqueue1(key: K, item: A): F[Option[(K, Stream[F, A])]] =
    withKey(key)(_.enqueue1(item.some))

  override val shutdownAll: F[Unit] =
    queues.get.flatMap(_.values.toList.traverse_(_.enqueue1(None)))

  private[this] def withKey(key: K)(
    use: NoneTerminatedQueue[F, A] => F[Unit]
  ): F[Option[(K, Stream[F, A])]] =
    queues.get.flatMap { qm =>
      qm.get(key)
        .fold {
          for { // No queue for key - create new one
            newQ <- Queue.noneTerminated[F, A]
            _ <- queues.update(x => x + (key -> newQ))
            _ <- use(newQ)
                } yield (key -> newQ.dequeue).some
        }(q => use(q).as(None))
    }

}

private class ItemBoundedKeyedEnqueue[F[_]: Monad, K, A](
                                                          ke: KeyedEnqueue[F, K, A],
                                                          limit: Semaphore[F]
                                                        ) extends KeyedEnqueue[F, K, A] {

  override def enqueue1(key: K, item: A): F[Option[(K, Stream[F, A])]] =
    limit.acquire >> ke
      .enqueue1(key, item)
      .map(_.map {
        case (key, stream) =>
          // We only need to attach the "release" behavior to a given stream once because each stream is emitted once, and then reused
          key -> stream.chunks
            .evalTap(c => limit.releaseN(c.size.toLong))
            .flatMap(Stream.chunk)
      })

  override val shutdownAll: F[Unit] = ke.shutdownAll
}
