package uk.co.odinconsultants.fp.scalaz

import scalaz.Monad

import Monads.Context


sealed trait MonadX[+A] {
  def run(ctx: Context): A
}

object MonadX {

  type M[X] = MonadX[X]

  var depth = 0 // yes, I know, I know. It's just for illustrative purposes

  def log(x: String): Unit = println((" " * (depth * 4)) + x)

  def apply[A](f: Context => A, name: String): M[A] = new M[A] {
    override def run(ctx: Context): A = {
      //println((new Exception()).getStackTrace.drop(1).take(15).map("\t" + _).mkString("\n"))
      log(s"$this.run ...")
      depth = depth + 1
      //log(s"calling $this.f(ctx) where f = $f")
      val fResult = f(ctx)
      log(s"$this.f(ctx) = $fResult")
      depth = depth - 1
      log(s"$this.run Finished")
      fResult
    }
    override def toString = name
  }

  implicit val monad = new Monad[MonadX] {
    override def bind[A, B](fa: M[A])(f: A ⇒ M[B]): M[B] = {
      log(s"Binding $fa, f=$f ...")
      MonadX(newF(fa, f), "bound" + fa.toString)
    }
    override def point[A](a: ⇒ A): M[A] = {
      log(s"Creating point ($a) [${a.getClass.getSimpleName}]")
      MonadX(_ ⇒ a, "point")
    }
    def newF[B, A](fa: M[A], f: A => M[B]): Context => B = { ctx ⇒
      val faRan:    A     = fa.run(ctx)
      log(s"calling f($faRan) where f = $f")
      depth = depth + 1
      val fResult:  M[B]  = f(faRan)
      depth = depth - 1
      log(s"f($faRan) = $fResult")
      fResult.run(ctx)
    }
  }
}

object Monads {
  import scalaz.Scalaz._
  def main(args: Array[String]): Unit = {
    val hello:    MonadX[String]  = MonadX(helloFn,     "hello")
    val hashcode: MonadX[Long]    = MonadX(hashCodeFn,  "hashCode")
/*Binding hello ...                                                 # boundhello is created with { ctx => ... f = { x <- hello }; fa = hello ... }, but nothing further called

About to run boundhello
=======================
boundhello.run ...
    hello.run ...
        hello.f(ctx) = bonjour
    hello.run Finished
    calling f(bonjour) where f = <function1>
        Binding hashCode, f=<function1> ...
    f(bonjour) = boundhashCode
    boundhashCode.run ...
        hashCode.run ...
            hashCode.f(ctx) = -1184959538
        hashCode.run Finished
        calling f(-1184959538) where f = <function1>
            Creating point (18) [Integer]
        f(-1184959538) = point
        point.run ...
            point.f(ctx) = 18
        point.run Finished
        boundhashCode.f(ctx) = 18
    boundhashCode.run Finished
    boundhello.f(ctx) = 18
boundhello.run Finished

* See Monad.map which says: map[A,B](fa: F[A])(f: A => B): F[B] = bind(fa)(a => point(f(a)))
     */
    println(underline("About to run for-comprehension"))
    // Ultimately, we need map and flatMap defined somewhere for the Scala compiler to process this for-comprehension.
    // They come from scalaz.syntax.FunctorOps.map and scalaz.syntax.BindOps.flatMaP
    val boundhello = for {
      x <- hello     // "Binding hello..."
      y <- hashcode
    } yield {
      //MonadX.log(s"Yielding. x = $x [${x.getClass.getName}], y = $y [${y.getClass.getName}]") // Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
      (x + y).length
    }

    val map = Map("hello" -> "bonjour")
    println(underline(s"About to run $boundhello"))
    val helloHash = boundhello.run(map)
    println()

    println(helloHash) // "18"
  }
  val helloFn = new Function1[Context, String] {
    override def apply(ctx: Context): String = ctx("hello")
    override def toString():          String = "helloFn"
  }
  val hashCodeFn = new Function1[Context, Long] {
    override def apply(ctx: Context): Long    = ctx.hashCode
    override def toString():          String  = "hashCodeFn"
  }
  type Context = Map[String, String]

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

}

