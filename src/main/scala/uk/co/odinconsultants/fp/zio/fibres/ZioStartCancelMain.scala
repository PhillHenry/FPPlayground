package uk.co.odinconsultants.fp.zio.fibres

import zio.{URIO, ZEnv, _}
import zio.blocking._

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
    val startCancel = for {
      z <- effectBlockingCancelable(sleeping)(guarantee)
      f <- z.fork
      _ <- f.interrupt
    } yield {
      "done"
    }

    startCancel.catchAll(e => URIO(e.printStackTrace())).map(_ => 0)
  }

}
