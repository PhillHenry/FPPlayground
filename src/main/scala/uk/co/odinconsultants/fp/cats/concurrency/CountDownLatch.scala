package uk.co.odinconsultants.fp.cats.concurrency

import cats.effect.Concurrent
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import cats.effect.implicits._

trait CountDownLatch[F[_]] {
  def await: F[Unit]
  def countDown: F[Unit]
}
object CountDownLatch {
  def create[F[_]: Concurrent](target: Int): F[CountDownLatch[F]] = {
    assert(target >= 1, "target must be >= 1")

    sealed trait State
    case class Waiting(n: Int, waitOn: Deferred[F, Unit]) extends State
    case object Done extends State

    Ref[F]
      .of[State](Waiting(target, Deferred.unsafe))
      .map { state =>
        new CountDownLatch[F] {
          def countDown =
            state
              .modify {
                case Done => Done -> ().pure[F]
                case Waiting(n, waitOn) =>
                  val newN = n - 1
                  if (newN == 0) Done -> waitOn.complete(())
                  else Waiting(newN, waitOn) -> ().pure[F]
              }
              .flatten.uncancelable

          def await =
            state.modify {
              case Done => Done -> ().pure[F]
              case s @ Waiting(_, waitOn) => s -> waitOn.get
            }.flatten
        }
      }
  }


}
