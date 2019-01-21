package uk.co.odinconsultants.fp.scalaz

import scalaz.Monad

import Monads.Context


sealed trait MonadX[+A] {
  def run(ctx: Context): A
}

object MonadX {

  def apply[A](f: Context => A, name: String): MonadX[A] = new MonadX[A] {
    override def run(ctx: Context): A = {
      println(s"Running $this ...")
      f(ctx)
    }
    override def toString = name
  }

  implicit val monad = new Monad[MonadX] {
    override def bind[A, B](fa: MonadX[A])(f: A ⇒ MonadX[B]): MonadX[B] = {
      println(s"Binding $fa ...")
      MonadX(newF(fa, f), "bound" + fa.toString)
    }
    override def point[A](a: ⇒ A): MonadX[A] = {
      println(s"point $a [${a.getClass.getSimpleName}]")
      MonadX(_ ⇒ a, "point")
    }
    def newF[B, A](fa: MonadX[A], f: A => MonadX[B]): Context => B = { ctx ⇒
        val faRan = fa.run(ctx)
        println(s"Running f($faRan)")
        val fd    = f(faRan)
        fd.run(ctx)
      }
  }

}

object Monads {
  import scalaz.Scalaz._
  def main(args: Array[String]): Unit = {
    val hello:    MonadX[String]  = MonadX({ ctx: Context => ctx("hello")   }, "hello")
    val hashcode: MonadX[Long]    = MonadX({ ctx: Context => ctx.hashCode() }, "hashCode")
    /*
Binding hello ...

About to run boundhello
=======================
Running boundhello ...            [f(ctx) in MonadX.run]
Running hello ...                 [fa.run(ctx) in newMonad]
Running f(bonjour)                *appears* to 'pull' in the second line of the for comprehension
Binding hashCode ...              [via map in for-comprehension - remember that map = flatMap + point *]
Running boundhashCode ...         [fd.run(ctx) in bind]
Running hashCode ...              [fa.run(ctx) but this time in the boundhashCode]
Running f(1768203508)             [fd = f(faRan)]
point bonjour1768203508 [String]  [the point in the map = flatMap + point equation *]
Running point ...                 [fd.run(ctx)]
Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]

* See Monad.map which says: map[A,B](fa: F[A])(f: A => B): F[B] = bind(fa)(a => point(f(a)))
     */
    println(underline("About to run for-comprehension"))
    // Ultimately, we need map and flatMap defined somewhere for the Scala compiler to process this for-comprehension.
    // They come from scalaz.syntax.FunctorOps.map and scalaz.syntax.BindOps.flatMaP
    val boundhello = for {
      x <- hello     // "Binding hello..."
      y <- hashcode
    } yield {
      println(s"Yielding. x = $x [${x.getClass.getName}], y = $y [${y.getClass.getName}]") // Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
      x + y
    }

    val map = Map("hello" -> "bonjour", "goodbye" -> "au revoir")
    println(underline(s"About to run $boundhello"))
    val helloHash: String = boundhello.run(map)
    println()

    println(helloHash) // "bonjour1768203508"
  }

  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

}

