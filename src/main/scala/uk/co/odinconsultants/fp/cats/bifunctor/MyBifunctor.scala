package uk.co.odinconsultants.fp.cats.bifunctor

import cats.Bifunctor

object MyBifunctor {

  case class MyException(x: String) extends Throwable(x)

  def biMappedStr(x: String): String = x + "bimapped!"

  def agnostic[F[Throwable, String]: Bifunctor](f: F[Throwable, String]): F[Throwable, String] = {
    import cats.implicits._
    f.bimap(x => MyException(x.getMessage), biMappedStr)
  }

}
