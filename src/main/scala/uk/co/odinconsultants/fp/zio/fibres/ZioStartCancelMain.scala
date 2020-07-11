package uk.co.odinconsultants.fp.zio.fibres

import zio.{URIO, ZEnv, _}

object ZioStartCancelMain extends zio.App {

  val sleeping = ZIO {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val guarantee: URIO[Any, Unit] = URIO {
    println("Guarantee ran")
  }

  def run(args: List[String]): URIO[ZEnv, Int] = {
    val startCancel: ZIO[Any, Nothing, UIO[Fiber.Status]] = for {
      z <- sleeping.ensuring(guarantee).fork
      _ <- z.interrupt //join.catchAll(e => URIO(e.printStackTrace()))
    } yield {
      z.status
    }

    startCancel.map(_ => 0)
  }

}
