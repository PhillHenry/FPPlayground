package uk.co.odinconsultants.fp.cats.applicatives

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object MyParApplicatives extends IOApp {

  def io(x: String): IO[String] = IO {
    println(s"IO: $x")
    x
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val ok1   = io("first")
    val ok2   = io("second")
    val fail  = IO.raiseError[String](new Exception("Yoiks!"))

    val applicatives = ok1 *> fail *> ok2

    applicatives.as(ExitCode.Success)
  }
}
