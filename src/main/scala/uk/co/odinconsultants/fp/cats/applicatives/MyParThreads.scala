package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.EitherNec
import cats.effect.IO

object MyParThreads {

  def doPrintLine(msg: String): IO[Unit] = IO { println(s"${Thread.currentThread().getName}: $msg")}

  def main(args: Array[String]): Unit = {
    import cats.implicits._

    val t1:  EitherNec[String, String] = Right("hello")
    val t2:  EitherNec[String, String] = Right("world")
    val t3:  EitherNec[String, String] = Right("ok")
    val tupled = (t1, t2, t3)
    //    println(tupled.parTupled)
    val x: EitherNec[String, (String, String, String)] = tupled.parTupled
    println(x)
  }
}
