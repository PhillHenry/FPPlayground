package uk.co.odinconsultants.fp.cats.concurrency

import cats.effect.Concurrent
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import cats.effect.implicits._

trait CountDownLatch[F[_]] {
  def await: F[Unit]
  def countDown: F[Unit]
}

/**
Fabio Labella @SystemFw 01:36
I do make countDown uncancelable, but that's the only meaningful difference
it's more lines than the semaphore, but a lot easier to reason about imho

Adam Rosien @arosien 01:38
agreed
the flatten part was the most non-obvious, but i knew the modify had to run an effect

Fabio Labella @SystemFw 01:39
the modify(...).flatten is always the same, so if you've seen it once you're done
also in this case you don't need to do anything for interruption really
but if you did, this is also better for that
in CE3, semaphores can be safe again
 */
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

  /*
  Loránd Szakács @lorandszakacs 09:55
@PhillHenry, the uncancelable method lives in the cats.effect.implicits._ not cats.implicits._
and has the F[_]: Bracket[F, E] constraint, so if you're using Concurrent[F] you should be good.

Fabio Labella @SystemFw 10:11
also Adam's version is better
   */

  sealed trait State[F[_]]
  case class Outstanding[F[_]](n: Long, whenDone: Deferred[F, Unit])
    extends State[F]
  case class Done[F[_]]() extends State[F]

  def adamsFSM[F[_]: Concurrent](n: Long): F[CountDownLatch[F]] =
    for {
      whenDone <- Deferred[F, Unit]
      state <- Ref.of[F, State[F]](Outstanding(n, whenDone))
    } yield new CountDownLatch[F] {
      def countDown(): F[Unit] =
        state.modify {
          case Outstanding(1, whenDone) => Done() -> whenDone.complete(())
          case Outstanding(n, whenDone) =>
            Outstanding(n - 1, whenDone) -> ().pure[F]
          case Done() => Done() -> ().pure[F]
        }.flatten

      def await(): F[Unit] =
        state.get.flatMap {
          case Outstanding(_, whenDone) => whenDone.get
          case Done()                   => ().pure[F]
        }
    }
}
