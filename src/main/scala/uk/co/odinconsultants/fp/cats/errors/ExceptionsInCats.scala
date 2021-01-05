package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{ExitCode, IO, IOApp, Resource}

object ExceptionsInCats extends IOApp {

  def resourceBlowsUpOnRelease(inputIO: IO[Int]): Resource[IO, Int] =
    Resource.make(inputIO)(x => IO {
      throw new Exception(releaseErrorMessage(x))
    })

  def releaseErrorMessage(x: Int) = s"release failing for $x"

  override def run(args: List[String]): IO[ExitCode] = {
    val resource:   Resource[IO, Int]           = resourceBlowsUpOnRelease(IO.pure(1))
    val onFinalize: Resource[IO, Int]           = resource.onFinalize(IO { println("onFinalize") })
    val io:         IO[Unit]                    = onFinalize.use(x => IO { println(s"x = $x")})
    val attempted:  IO[Either[Throwable, Unit]] = io.attempt
    val handled:    IO[Unit]                    = io.handleErrorWith(t => IO { println(s"throwable = $t")} )
    (handled *> attempted *> IO { println("politely finished")}).as(ExitCode.Success)
  }
}
