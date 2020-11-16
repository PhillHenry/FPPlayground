package uk.co.odinconsultants.fp.lang

class TypeVsContextBounds[F[_]] {

  def widenTypeBound[A, B >: A](fa: F[A]): F[B] = ???

  def widenContextBound[A, B](fa: F[A])(implicit ev: A <:< B): F[B] = ???

}
