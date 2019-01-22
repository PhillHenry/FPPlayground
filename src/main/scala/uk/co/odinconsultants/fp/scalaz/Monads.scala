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
      log(s"$this.run")
      depth = depth + 1
      log(s"$f(ctx) = ")
      depth = depth + 1
      val fResult = f(ctx)
      log(s"'$fResult'")
      depth = depth - 1
      depth = depth - 1
      log(s"$this.run Finished")
      fResult
    }
    override def toString = name
  }

  implicit val monad = new Monad[MonadX] {
    override def bind[A, B](fa: M[A])(f: A ⇒ M[B]): M[B] = {
      log(s"bind: Creating bound$fa with fa=$fa, f=$f")
      val name = "bound" + fa.toString
      MonadX(newF(fa, f, name), name)
    }
    override def point[A](a: ⇒ A): M[A] = {
      log(s"Creating point ($a) [${a.getClass.getSimpleName}]")
      val fn = new Function1[Context, A] {
        override def apply(v1: Context): A = a
        override def toString(): String = "point.f"
      }
      MonadX(fn, "point")
    }
    def newF[B, A](fa: M[A], f: A => M[B], name: String): Context => B = new Function1[Context, B] {
      def apply(ctx: Context) = {
        val faRan:    A     = fa.run(ctx)
        log(s"$f($faRan) = ")
        depth = depth + 1
        val fResult:  M[B]  = f(faRan)
        log(s"'$fResult'")
        depth = depth - 1
        fResult.run(ctx)
      }

      override def toString(): String = s"$name.f"
    }
  }
}

object Monads {
  import scalaz.Scalaz._
  def main(args: Array[String]): Unit = {
    val hello:    MonadX[String]  = MonadX(helloFn,           "Hello")
    val hashcode: MonadX[Long]    = MonadX(meaningOfLifeFn,   "MeaningOfLife")
/*
About to run for-comprehension
==============================
bind: Creating boundHello with fa=Hello, f=<function1>

About to run boundhello
=======================
boundHello.run
    boundHello.f(ctx) =
        Hello.run
            helloFn(ctx) =
                'hello'
        Hello.run Finished
        <function1>(hello) =
            bind: Creating boundMeaningOfLife with fa=MeaningOfLife, f=<function1>
            'boundMeaningOfLife'
        boundMeaningOfLife.run
            boundMeaningOfLife.f(ctx) =
                MeaningOfLife.run
                    meaningOfLifeFn(ctx) =
                        '42'
                MeaningOfLife.run Finished
                <function1>(42) =
                    Creating point (7) [Integer]
                    'point'
                point.run
                    point.f(ctx) =
                        '7'
                point.run Finished
                '7'
        boundMeaningOfLife.run Finished
        '7'
boundHello.run Finished


* See Monad.map which says: map[A,B](fa: F[A])(f: A => B): F[B] = bind(fa)(a => point(f(a)))
     */
    println(underline("About to run for-comprehension"))
    // Ultimately, we need map and flatMap defined somewhere for the Scala compiler to process this for-comprehension.
    // They come from scalaz.syntax.FunctorOps.map and scalaz.syntax.BindOps.flatMap
    val boundhello: MonadX[Int] = for {
      x <- hello     // "Binding hello..."
      y <- hashcode
    } yield {
      //MonadX.log(s"Yielding. x = $x [${x.getClass.getName}], y = $y [${y.getClass.getName}]") // Yielding. x = bonjour [java.lang.String], y = 1768203508 [long]
      (x + y).length
    }

    val map = Context("hello", 42)
    println(underline(s"About to run $boundhello"))
    val helloHash = boundhello.run(map)
    println()

    println(s"$helloHash of type ${helloHash.getClass.getSimpleName}")
  }
  val helloFn = new Function1[Context, String] {
    override def apply(ctx: Context): String = ctx.aString
    override def toString():          String = "helloFn"
  }
  val meaningOfLifeFn = new Function1[Context, Long] {
    override def apply(ctx: Context): Long    = ctx.aLong
    override def toString():          String  = "meaningOfLifeFn"
  }
  case class Context(aString: String, aLong: Long)

  def underline(x: String): String = "\n" + x + "\n" + ("=" * x.length)

}

