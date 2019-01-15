package uk.co.odinconsultants.fp.tmorris

/**
  * http://blog.tmorris.net/posts/monads-do-not-compose/
  */
object MonadsDoNotCompose extends App {

  trait Functor[F[_]] {
    def fmap[A, B](f: A => B, a: F[A]): F[B]
  }

  trait Applicative[F[_]] extends Functor[F] {
    def ap[A, B](f: F[A => B], a: F[A]): F[B]
    def point[A](a: A): F[A]
    override final def fmap[A, B](f: A => B, a: F[A]) =
      ap(point(f), a)
  }

  trait Monad[F[_]] extends Applicative[F] {
    def flatMap[A, B](f: A => F[B], a: F[A]): F[B]
    override final def ap[A, B](f: F[A => B], a: F[A]) =
      flatMap((ff: A => B) => fmap((aa: A) => ff(aa), a), f)
  }

//  def XCompose[M[_], N[_]](implicit mx: X[M], nx: X[N]):
//    X[({type λ[α]=M[N[α]]})#λ]

  def FunctorCompose[M[_], N[_]](implicit mx: Functor[M], nx: Functor[N]): Functor[({type λ[α]=M[N[α]]})#λ] =
  new Functor[({type λ[α]=M[N[α]]})#λ] {
    def fmap[A, B](f: A => B, a: M[N[A]]) =
      mx.fmap((na: N[A]) => nx.fmap(f, na), a)
  }

  def ApplicativeCompose[M[_], N[_]]
  (implicit ma: Applicative[M], na: Applicative[N]):
  Applicative[({type λ[α]=M[N[α]]})#λ] =
    new Applicative[({type λ[α]=M[N[α]]})#λ] {
      def ap[A, B](f: M[N[A => B]], a: M[N[A]]) = {
        def liftA2[X, Y, Z](f: X => Y => Z, a: M[X], b: M[Y]): M[Z] =
          ma.ap(ma.fmap(f, a), b)
        liftA2((ff: N[A => B]) => (aa: N[A]) => na.ap(ff, aa), f, a)
      }
      def point[A](a: A) =
        ma point (na point a)
    }

}
