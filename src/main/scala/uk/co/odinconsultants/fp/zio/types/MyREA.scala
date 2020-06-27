package uk.co.odinconsultants.fp.zio.types
import zio.{IO, Task, ZEnv, ZIO}

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

    val R: MyA with MyB = new MyA with MyB {}
    val x: IO[Throwable, String] = result.provide(R)

    val myEnv:      ZIO[Any, Throwable, MyA with MyB] = ZIO(R)
    val massaged:   ZIO[Any, Throwable, String]       = result.compose(myEnv)

    massaged.fold(_ => 1, _ => 0)
  }
}
