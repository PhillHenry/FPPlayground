package uk.co.odinconsultants.fp.cats.state

import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.Stream

object FlatMapQueryMain {

  def main(args: Array[String]): Unit = {
    val testStream = Set(1, 2, 3, 4, 5)
//    testStream.flatMap { state => // The below block is executed
    for (state <- testStream) yield { // The below block is NOT executed
      println("Hello, world")
    }
  }


  def fs2(): Unit = {
    val nReadCommitted  = Ref[IO].of(0)
    val testStream      = Stream.eval(nReadCommitted)
//    testStream.flatMap { state =>   // The below block is executed
    for (state <- testStream) yield { // The below block is NOT executed
      println("Hello, world")
      testStream
    }.compile.drain.unsafeRunSync()
  }

}
