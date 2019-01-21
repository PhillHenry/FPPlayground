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
      println((new Exception()).getStackTrace.drop(1).take(10).map("\t" + _).mkString("\n"))
      MonadX(ctx ⇒ f(fa.run(ctx)).run(ctx), "bound" + fa.toString)
    }

    override def point[A](a: ⇒ A): MonadX[A] = MonadX(_ ⇒ a, "point")
  }

}

object Monads {

  import scalaz.Scalaz._

  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

  def main(args: Array[String]): Unit = {
    val helloMonad: MonadX[String] = MonadX({ ctx: Context =>
      ctx("hello")
    }, "hello")
    val hashCodeMonad: MonadX[Long] = MonadX ({ ctx: Context =>
      ctx.hashCode()
    }, "hashCode")

    println(underline("About to run for-comprehension"))
    // Ultimately, we need map and flatMap defined somewhere for the Scala compiler to process this for-comprehension.
    // They come from scalaz.syntax.FunctorOps.map and scalaz.syntax.BindOps.flatMa
    val helloHashMonad = for {
        x <- helloMonad     // "Binding hello..."
        y <- hashCodeMonad
    } yield {
      println(s"Yielding. x = $x [${x.getClass.getName}], y = $y [${y.getClass.getName}]") // Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
      x + y
    }

    val map = Map("hello" -> "bonjour", "goodbye" -> "au revoir")
    println(underline(s"About to run $helloHashMonad"))
    /*
About to run boundhello
=======================
Running boundhello ...
Running hello ...
Binding hashCode ...
Running boundhashCode ...
Running hashCode ...
Running point ...
Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
     */
    val helloHash: String = helloHashMonad.run(map)
    println()

    println(helloHash) // "bonjour1768203508"
  }

}

