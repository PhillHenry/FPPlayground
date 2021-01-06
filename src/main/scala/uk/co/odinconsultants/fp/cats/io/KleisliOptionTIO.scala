package uk.co.odinconsultants.fp.cats.io

import cats.data.{Kleisli, OptionT}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

/**
 * Basically, like HTTP4s
 */
object KleisliOptionTIO extends IOApp {

  case class Request[F[_]](f: F[_])
  case class Response[F[_]](f: F[_])

  type Result = Option[Response[IO]]
  type Http4sIOOption = IO[Result]

  def someResult(x: Int): Result = Some(Response( IO {
    println(s"result = $x")
    Some(x)
  }))

  def ioPrint(x: Int): Http4sIOOption = IO {
    println(x)
    someResult(x)
  }

  def printIO(x: String): IO[Unit] = IO { println(x) }

  type Http4sIOOptionT = OptionT[IO, Response[IO]]

  val optionTIOInt1: Http4sIOOptionT = OptionT(ioPrint(1))
  val optionTIOInt2: Http4sIOOptionT = OptionT(ioPrint(2))

  override def run(args: List[String]): IO[ExitCode] = {
    type Http4sKleisli = Kleisli[OptionT[IO, *], Request[IO], Response[IO]]

    val x: Http4sKleisli = Kleisli(_ => optionTIOInt1)
    val y: Http4sKleisli = Kleisli(_ => optionTIOInt2)
    (y <+> x).run(Request(printIO("request"))).value.as(ExitCode.Success) // note that only the first Http4sKleisli is run
  }

}
