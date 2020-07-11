package uk.co.odinconsultants.fp.zio.fibres

import zio.{URIO, ZEnv}
import zio._

object ZioStartCancelMain extends zio.App {

  val sleeping = ZIO {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val guarantee = ZIO {
    println("Guarantee ran")
  }

  def run(args: List[String]): URIO[ZEnv, Int] = {
    val startCancel = for {
      z <- sleeping.fork.flatMap(_.interrupt)
    } yield {
      z.interrupted
    }

    startCancel.map(if (_) 1 else 0)
  }

}
