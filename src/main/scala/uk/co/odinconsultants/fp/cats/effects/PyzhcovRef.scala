package uk.co.odinconsultants.fp.cats.effects

import cats.implicits._
import cats.effect._, concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.Implicits._
import cats.effect.IOApp

/**
  * See https://olegpy.com/things-to-store-in-a-ref/
  */
object PyzhcovRef extends IOApp {


  override def run(args: List[String]): IO[ExitCode] = {
    val x = for {
      ref  <- Ref[IO].of(0)
      read <- periodicReader(ref).start
      incr <- periodicIncrementer(ref).start
      _    <- IO.sleep(10.seconds)
      _    <- read.cancel
      _    <- incr.cancel
    } yield ()
    x.map(_ => ExitCode(0))
  }

  def periodicReader(ref: Ref[IO, Int]): IO[Unit] =
    IO.sleep(1.second) >> ref.get.flatMap(i => IO(println(s"Current value is $i"))) >> periodicReader(ref)

  def periodicIncrementer(ref: Ref[IO, Int]): IO[Unit] =
    IO.sleep(750.millis) >> ref.update(_ + 1) >> periodicIncrementer(ref)



}
