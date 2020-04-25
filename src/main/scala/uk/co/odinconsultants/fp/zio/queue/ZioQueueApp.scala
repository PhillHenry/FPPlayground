package uk.co.odinconsultants.fp.zio.queue

import zio.console.putStrLn
import zio.{App, Queue, ZIO}
import zio.duration._

/**
 * "I want to repeat until the queue is empty (or while the queue has values)"
 * PyAntony 24/4/20 at 11:42 PM
 */
object ZioQueueApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    for {
      q <- Queue.bounded[Int](100)
      _ <- q.offerAll((0 to 10)).fork
      e =  q.take.flatMap(n => putStrLn(n.toString) *> ZIO.sleep(1.second))
      _ <- e.doUntilM(_ => q.size.map(_ == 0))
    } yield 0

}
