package uk.co.odinconsultants.fp.zio.fibres

import zio.{URIO, ZEnv, _}
import zio.blocking._

object ZioStartCancelMain extends zio.App {

  val sleepingZIO = ZIO {
    sleeping
  }

  private def sleeping = {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val guarantee: URIO[Any, Unit] = URIO {
    println("Guarantee ran")
  }

  def run(args: List[String]): URIO[ZEnv, Int] = {
    val z = effectBlockingCancelable(sleeping)(guarantee)
    val startCancel: ZIO[Blocking, Nothing, String] = for {
      f <- z.fork
      _ <- f.interrupt
    } yield {
      "done"
    }

    startCancel.map(_ => 0)
  }

}
