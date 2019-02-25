package uk.co.odinconsultants.fp.cats.state

import cats._
import cats.implicits._
//import cats.effect.{IO, Timer}
import cats.effect._
import cats.effect.concurrent.Ref


object FooAlg {

  case class Foo(x: String)

  trait FooAlg[F[_]] {
    def get: F[Foo]  // translate into business term
    def set(foo: Foo): F[Unit]
  }

  def main(args: Array[String]): Unit = {
    println("hello")
  }

  def create[F[_]: Sync]: F[FooAlg[F]] =
    Ref.of[F, Foo](Foo("test")).map { ref =>
      new FooAlg[F] {
        def get: F[Foo] = ref.get
        def set(foo: Foo): F[Unit] = ref.set(foo)
      }
    }
}