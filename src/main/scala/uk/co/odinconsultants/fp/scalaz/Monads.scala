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

sealed trait MonadY[+A] {
  def run(ctx: Context): A
}

object MonadY {

  def apply[A](f: Context => A): MonadY[A] = new MonadY[A] {
    override def run(ctx: Context): A = f(ctx)
  }

  implicit val monad = new Monad[MonadY] {
    override def bind[A, B](fa: MonadY[A])(f: A ⇒ MonadY[B]): MonadY[B] =
      MonadY(ctx ⇒ f(fa.run(ctx)).run(ctx))

    override def point[A](a: ⇒ A): MonadY[A] = MonadY(_ ⇒ a)
  }

}

object Monads {

  import scalaz.Scalaz._

  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

  def main(args: Array[String]): Unit = {
    val monadx: MonadX[String] = MonadX({ ctx: Context =>
      ctx("hello")
    }, "hello")
    val monadx2: MonadX[Long] = MonadX ({ ctx: Context =>
      ctx.hashCode()
    }, "hashCode")

    val monady: MonadY[String] = MonadY { str =>
      str + ", Phillip"
    }

    println(underline("About to run for-comprehension"))
    // Ultimately, we need map and flatMap defined somewhere for the Scala compiler to process this for-comprehension.
    // They come from scalaz.syntax.FunctorOps.map and scalaz.syntax.BindOps.flatMa
    val helloHashMonad = for {
        x <- monadx   // "Binding hello..."
        y <- monadx2
    } yield x + y

    val map = Map("hello" -> "bonjour", "goodbye" -> "au revoir")
    println(underline("About to run"))
    /*
About to run
============
Running boundhello ...
Running hello ...
Binding hashCode ...
Running boundhashCode ...
Running hashCode ...
Running point ...
     */
    val helloHash: String = helloHashMonad.run(map)
    println()

    println(helloHash) // "bonjour1768203508"
  }

}

