package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{ExitCode, IO, IOApp, Resource}

object ExceptionsInCats extends IOApp {

  def resourceBlowsUpOnRelease(inputIO: IO[Int]): Resource[IO, Int] =
    Resource.make(inputIO)(x => IO {
      throw new Exception(releaseErrorMessage(x))
    })

  def releaseErrorMessage(x: Int) = s"release failing for $x"

  override def run(args: List[String]): IO[ExitCode] = {
    val io = resourceBlowsUpOnRelease(IO.pure(1)).use(x => IO { println(s"x = $x")})
    val x = io.attempt
    (x *> IO { println("politely finished")}).as(ExitCode.Success)
  }
}
