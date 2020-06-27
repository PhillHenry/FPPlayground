package uk.co.odinconsultants.fp.zio.types
import zio.{Task, ZEnv, ZIO}

object MyREA extends zio.App {

  trait MyA
  trait MyB

  val zioA: ZIO[MyA, Throwable, String] = ZIO("hello")
  val zioB: ZIO[MyB, Throwable, String] = ZIO("hello")

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val result: ZIO[MyB with MyA, Throwable, String] = for {
      a <- zioA
      b <- zioB
    } yield a + b

    val x: ZIO[Any, Throwable, Unit] = ZIO(println("Hello"))

    val myEnv: ZIO[Any, Throwable, MyA with MyB] = ZIO(new MyA with MyB{})

    val massaged: ZIO[MyB with MyA, Throwable, String] = (zioA *> zioB)
    massaged.compose(myEnv).fold(_ => 1, _ => 0)
  }
}
