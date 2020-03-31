package uk.co.odinconsultants.fp.cats.validation

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object KleisliValidation extends IOApp {

  case class FailureReason(someInt: Int)
  case class DomainThingy(someStr: Int)
  case class TransactionEvent(someStr: String, someInt: Int)

  val checkFirstThing: Kleisli[Option, Int, FailureReason] = ???
  val checkSecondThing: Kleisli[Option, String, FailureReason] = ???
  val checkAllThings: Kleisli[Option, TransactionEvent, FailureReason] =
    NonEmptyList.of[Kleisli[Option, TransactionEvent, FailureReason]](
      checkFirstThing.local(_.someInt),
      checkSecondThing.local(_.someStr)
    )
      .reduceLeft(_ <+> _)

  override def run(args: List[String]): IO[ExitCode] = {
    IO(println(checkAllThings)).as(ExitCode.Success)
  }
}
