package uk.co.odinconsultants.fp.scalaz.io

import scalaz.Monad
import scalaz.Scalaz._
import scalaz._, Scalaz._, effect._, IO._
import scalaz.zio.IO

/**
  * @see http://degoes.net/articles/effects-without-transformers
  */
object HighPerformanceEffects {

  trait MonadState[F[_], S] extends Monad[F] { self =>
    def get: F[S]
    def put(s: S): F[Unit]
  }

  case class AppState(appState: String)

  def runApp[F[_]](implicit F: MonadState[F, AppState]): F[Unit] =
    for {
      state       <- F.get
      newState    <- F.put(state.copy(appState = state.appState))
    } yield newState

  type MyError = Exception

  class MyIO[A](val run: IO[MyError, A]) extends AnyVal

  // doesn't compile with Zio 0.5.3
//  def createMonadState[E, S](initial: S): IO[E, MonadState[MyIO, S]] =
//    for {
//      ref <- IORef(initial)
//    } yield new MonadState[MyIO, S] {
//      def get: MyIO[S] = MyIO(ref.read)
//      def put(s: S): MyIO[Unit] = MyIO(ref.write(s))
//    }

  def main(args: Array[String]): Unit = {
    println("This is not FP!")
  }

}
