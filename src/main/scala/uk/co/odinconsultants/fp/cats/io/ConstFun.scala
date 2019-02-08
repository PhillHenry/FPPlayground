package uk.co.odinconsultants.fp.cats.io

/**
  * @see https://typelevel.org/cats/datatypes/const.html
  */

import cats.{Functor, Id}
import cats.data.Const

trait Lens[S, A] {
  def modifyF[F[_] : Functor](s: S)(f: A => F[A]): F[S]

  def set(s: S, a: A): S = modify(s)(_ => a)

  def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

  def get(s: S): A
}

object ConstFun {

  implicit def constFunctor[X]: Functor[Const[X, ?]] =
    new Functor[Const[X, ?]] {
      // Recall Const[X, A] ~= X, so the function is not of any use to us
      def map[A, B](fa: Const[X, A])(f: A => B): Const[X, B] =
        Const(fa.getConst)
    }

}
