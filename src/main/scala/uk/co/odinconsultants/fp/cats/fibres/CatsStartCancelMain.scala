package uk.co.odinconsultants.fp.cats.fibres

import cats.effect.{ExitCode, IO, IOApp}

object CatsStartCancelMain extends IOApp {

  val sleeping = IO {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val guarantee = IO {
    println("Guarantee ran")
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val result = for {
      x <- sleeping.guarantee(guarantee).start.flatMap(_.cancel)
    } yield {
      x
    }

    result.map(_ => ExitCode.Success)
  }
}
