package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.NonEmptyList
import cats.{Applicative, ApplicativeError}
import java.io.{InputStream, OutputStream}

import scala.util.Try

class MyApplicativeErrors[F[_]: Applicative](implicit E: ApplicativeError[F, Throwable]) {

  def doIO[U](f: => U): F[U] = E.fromTry(Try(f))

  def allOrNothing[A](xs: NonEmptyList[F[A]]): F[A] = {
    import cats.implicits._
    xs.foldLeft(xs.head) { case (a, x) =>
      a *> x
    }
  }

  def doIO(firstOutputStream: OutputStream, secondOutputString: OutputStream, inputStream: InputStream): F[Unit] = {
    val fo1 = doIO(firstOutputStream.close())
    val fo2 = doIO(secondOutputString.close())
    val fi = doIO(inputStream.close())

    allOrNothing(NonEmptyList(fo1, List(fo2, fi)))
  }

  def pureHappyPath[U](u: U): F[U] = E.pure(u)

  override def toString(): String = s"${this.getClass.getName}: E = $E (${E.getClass.getCanonicalName})"
}

object MyApplicativeErrors {

  def main(args: Array[String]): Unit = {
    type MyType[T] = Either[Throwable, T]
    import cats.implicits._
    val x = new MyApplicativeErrors[MyType]
    println(x)
  }
}
