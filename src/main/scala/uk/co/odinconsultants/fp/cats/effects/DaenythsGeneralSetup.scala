package uk.co.odinconsultants.fp.cats.effects

import cats.effect.{ExitCode, IO, IOApp, Resource}

object DaenythsGeneralSetup extends IOApp {

  case class MyConfig(todo: String)

  def buildApp(cfg: MyConfig): Resource[IO, MyApp] = ???

  def readConfig: IO[MyConfig] = ??? // in case parsing fails etc

  def run(args: List[String]): IO[ExitCode] = {
    val io: IO[Unit] = Resource.liftF(readConfig).flatMap(buildApp).use(_.run)
    io.map(_ => ExitCode.Success)
  }

  trait MyApp { def run: IO[Unit] }
  // or def run: Stream[IO, Unit]
//  class MyAppImpl(db: doobie.Transactor[IO], http: http4s.client.Client[IO], ........) extends MyApp { ??? }

}
