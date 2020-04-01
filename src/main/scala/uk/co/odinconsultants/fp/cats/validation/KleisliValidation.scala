package uk.co.odinconsultants.fp.cats.validation

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

/**
Brian P. Holt @bpholt Mar 30 19:53
So None is the success case?

Ryan Peters @sloshy Mar 30 19:53
Yep - if it's None then whatever I've validated just gets passed along through
Only thing I don't really like is for it to compile I need to specify the type on NonEmptyList.of

Brian P. Holt @bpholt Mar 30 19:54
Oh, that's cool

Ryan Peters @sloshy Mar 30 19:54
Maybe there's a better way to tell the compiler what I want

Brian P. Holt @bpholt Mar 30 19:54
(the passthrough, I mean)

Ryan Peters @sloshy Mar 30 19:57
checkAllThings
  .run(domainThingy)
  .map(doSomethingWithError)
  .getOrElse(happyPath)
 */
object KleisliValidation extends IOApp {

  case class FailureReason(someInt: Int, reason: String)
  case class TransactionEvent(txStr: String, txInt: Int)

  val checkInt:     Kleisli[Option, Int,    FailureReason] = Kleisli(x => Some(FailureReason(x, "default reason")))
  val checkString:  Kleisli[Option, String, FailureReason] = Kleisli(x => Some(FailureReason(-1, x)))

  val checkAllThings:   Kleisli[Option, TransactionEvent, FailureReason]  =
    NonEmptyList.of[Kleisli[Option, TransactionEvent, FailureReason]](
      checkString .local(_.txStr),
      checkInt    .local(_.txInt)
  ).reduceLeft(_ <+> _)

  override def run(args: List[String]): IO[ExitCode] = {
//    println(checkFirstThing(1)) // Some(FailureReason(1,default reason))

    val txEvent = TransactionEvent("a tx event", 42)
    println(checkAllThings(txEvent))
    IO(ExitCode.Success)
  }
}
