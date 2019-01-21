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

    override def point[A](a: ⇒ A): MonadX[A] = {
      println(s"point $a [${a.getClass.getSimpleName}]")
      println((new Exception()).getStackTrace.drop(1).take(15).map("\t" + _).mkString("\n"))
      MonadX(_ ⇒ a, "point")
    }
  }

}

object Monads {

  import scalaz.Scalaz._

  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

  def main(args: Array[String]): Unit = {
    val hello: MonadX[String] = MonadX({ ctx: Context =>
      println("=>\tctx(hello)")
      ctx("hello")
    }, "hello")
    val hashcode: MonadX[Long] = MonadX ({ ctx: Context =>
      println("=>\thashCode")
      ctx.hashCode()
    }, "hashCode")

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
    /*
About to run boundhello
=======================
Running boundhello ...            [f(ctx) in run]
Running hello ...                 [fa.run(ctx) in bind]
Binding hashCode ...              [via map in for-comprehension - remember that map = flatMap + point]
Running boundhashCode ...         [f(...).run(ctx) in bind]
Running hashCode ...
point bonjour1768203508 [String]
Running point ...
Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
     */
    val helloHash: String = boundhello.run(map)
    println()

    println(helloHash) // "bonjour1768203508"
  }

}

