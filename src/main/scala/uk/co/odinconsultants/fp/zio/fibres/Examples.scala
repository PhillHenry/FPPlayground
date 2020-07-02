package uk.co.odinconsultants.fp.zio.fibres

import zio.duration._
import zio._

/**
kamil06/30/2020
@adamfraser 1. Interruption is not checked on map 2. Some computations are heavy (ML)
adamfraser06/30/2020
@kamil It is checked. For example in the following snippet the debug statement in the second map will never run:
 */
object Example extends zio.App {

  def run(args: List[String]): URIO[ZEnv, Int] =
    for {
      fiber <- myProgram.fork
      _     <- ZIO.sleep(1.second)
      _     <- fiber.interrupt
    } yield 0

  val myProgram =
    ZIO
      .effect(println("Starting"))
      .map { _ =>
        println("Starting some expensive compution")
        val start = System.currentTimeMillis()
        Thread.sleep(2000)
        println(s"Finishing with expensive computation in ${System.currentTimeMillis() - start} ms")
      }
      .map(_ => println("I shouldn't be doing effects in map but for illutrative purposes"))
}
