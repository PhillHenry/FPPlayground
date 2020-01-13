package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object SendAndReceiveMain extends IOApp {

  import ProducerKafka._
  import ConsumerKafka._

  override def run(args: List[String]): IO[ExitCode] = {
    val consume: IO[Unit] = cStream(printMessage).compile.drain
    val produce: IO[Unit] = pStream.compile.drain
    (consume, produce).parMapN((_, _)=> ExitCode.Success)
  }
}
