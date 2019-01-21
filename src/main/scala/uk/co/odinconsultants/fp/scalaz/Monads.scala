package uk.co.odinconsultants.fp.scalaz

import scalaz.Monad

import Monads.Context


sealed trait MonadX[+A] {
  def run(ctx: Context): A
}

object MonadX {

  def apply[A](f: Context => A): MonadX[A] = new MonadX[A] {
    override def run(ctx: Context): A = f(ctx)
  }

  implicit val monad = new Monad[MonadX] {
    override def bind[A, B](fa: MonadX[A])(f: A ⇒ MonadX[B]): MonadX[B] =
      MonadX(ctx ⇒ f(fa.run(ctx)).run(ctx))

    override def point[A](a: ⇒ A): MonadX[A] = MonadX(_ ⇒ a)
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

  def main(args: Array[String]): Unit = {
    val monadx: MonadX[String] = MonadX { ctx: Context =>
      ctx("hello")
    }
    val monadx2: MonadX[Long] = MonadX { ctx: Context =>
      ctx.hashCode()
    }
    val monady: MonadY[String] = MonadY { str =>
      str + ", Phillip"
    }

    val salutation = for {
        x <- monadx
          y <- monadx2
    } yield x + y

    val map = Map("hello" -> "bonjour", "goodbye" -> "au revoir")
    println(monadx.run(map)) // bonjour
    println(salutation.run(map))
  }

}

