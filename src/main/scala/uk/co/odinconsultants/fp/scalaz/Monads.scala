package uk.co.odinconsultants.fp.scalaz

import scalaz.Monad

import Monads.Context


sealed trait MonadX[+A] {
  def run(ctx: Context): A
}

object MonadX {

  type M[X] = MonadX[X]

  def apply[A](f: Context => A, name: String): M[A] = new M[A] {
    override def run(ctx: Context): A = {
      println(s"Running $this ...")
      f(ctx)
    }
    override def toString = name
  }

  implicit val monad = new Monad[MonadX] {
    override def bind[A, B](fa: M[A])(f: A ⇒ M[B]): M[B] = {
      println(s"Binding $fa ...")
      MonadX(newF(fa, f), "bound" + fa.toString)
    }
    override def point[A](a: ⇒ A): M[A] = {
      println(s"point $a [${a.getClass.getSimpleName}]")
      MonadX(_ ⇒ a, "point")
    }
    def newF[B, A](fa: M[A], f: A => M[B]): Context => B = { ctx ⇒
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
Running hello ...                 [fa.run(ctx) in newF = 'bonjour']
Running f(bonjour)                f *appears* to 'pull' in the second line of the for comprehension
Binding hashCode ...              [via map in for-comprehension - remember that map = flatMap + point *]
Running boundhashCode ...         [fd.run(ctx) in newF of boundhello]
Running hashCode ...              [f(ctx) in MonadX.run but this time from in boundhashCode.newF's fa.run(ctx)]
Running f(1768203508)             [simply from hashcode.run]
point 18 [Integer]                f *appears* to pull in the point in the map = flatMap + point equation *
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
      (x + y).length
    }

    val map = Map("hello" -> "bonjour")
    println(underline(s"About to run $boundhello"))
    val helloHash = boundhello.run(map)
    println()

    println(helloHash) // "18"
  }

  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

}

