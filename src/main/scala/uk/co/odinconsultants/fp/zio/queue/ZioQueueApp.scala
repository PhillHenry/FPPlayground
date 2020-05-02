package uk.co.odinconsultants.fp.zio.queue

import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.{App, Fiber, Queue, UIO, URIO, ZIO}
import zio.duration._

/**
 * "I want to repeat until the queue is empty (or while the queue has values)"
 * PyAntony 24/4/20 at 11:42 PM
 */
object ZioQueueApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val x: ZIO[Clock with Console, Nothing, Int] = for {
      q <- createQueue
      _ <- populateQueue(q)
      e =  takeLogAndSleep(q)
      _ <- drain(e, q)
    } yield 0
    x
  }

  private def createQueue: UIO[Queue[Int]] =
    Queue.bounded[Int](100)

  private def populateQueue(q: Queue[Int]): URIO[Any, Fiber.Runtime[Nothing, Boolean]] =
    q.offerAll((0 to 10)).fork

  private def takeLogAndSleep(q: Queue[Int]): ZIO[Clock with Console, Nothing, Unit] =
    q.take.flatMap(n => putStrLn(n.toString) *> ZIO.sleep(1.second))

  private def drain(e: ZIO[Clock with Console, Nothing, Unit],
                    q: Queue[Int]): ZIO[Clock with Console, Nothing, Unit] =
    e.doUntilM(_ => q.size.map(_ == 0))

}
