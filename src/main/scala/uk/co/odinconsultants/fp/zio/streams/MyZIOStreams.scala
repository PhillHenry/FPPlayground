package uk.co.odinconsultants.fp.zio.streams


import zio.blocking.{Blocking, effectBlocking, effectBlockingInterrupt}
import zio.clock.Clock
import zio.duration._
import zio.stream._
import zio._


object MyZIOStreams {

  val fibs: scala.Stream[Int] = 0 #:: fibs.scanLeft(1)(_ + _)

  val zioPrint = ZIO(println("Hello world!"))

  val repeatingPrint: ZStream[Any, Throwable, Unit] = ZStream.repeatEffect(zioPrint)

  val fibonacciStream: ZStream[Any, Throwable, Int] = ZStream.fromIterator(ZIO(fibs.iterator))

  val interleaved = repeatingPrint.interleave(fibonacciStream)

  val concatenated = repeatingPrint ++ fibonacciStream

  val printForkFib = repeatingPrint.drainFork(fibonacciStream)
  val fibForkPrint = fibonacciStream.drainFork(repeatingPrint)

}
