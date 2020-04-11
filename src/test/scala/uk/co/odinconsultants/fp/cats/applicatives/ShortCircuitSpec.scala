package uk.co.odinconsultants.fp.cats.applicatives

import cats.Applicative
import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated, ValidatedNec}
import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import cats.implicits._
import uk.co.odinconsultants.fp.cats.validation.ValidatedFixture

class ShortCircuitSpec extends WordSpec with Matchers {

  class ExpectedException extends Throwable
  object ExpectedException extends ExpectedException


  "Applicatives" should {
    type ApplicativeType = ValidatedNec[Throwable, Int]
    def allOrNothing[F[_]: Applicative, A](xs: NonEmptyList[F[A]]): F[A] = {
      xs.foldLeft(xs.head) { case (a, x) =>
        a *> x
      }
    }
    "not short circuit" in new ValidatedFixture {
      var i = 0
      def failure(): Validated[String, String] = {
        println("failure")
        i = i + 1
        invalid1
      }
      def success(): Validated[String, String] = {
        println("valid")
        i = i + 1
        valid1
      }
      success() *> failure() *> success()
      i shouldBe 3
    }
  }

  "Monadic effects" should {
    type MonadType = IO[Int]
    val aye: MonadType = IO {
      println("aye")
      42
    }
    val nay: MonadType = IO.raiseError(ExpectedException)
    "short-circuit if one nay" in {
      val io = for {
        result    <- aye *> nay
        expected  <- nay
      } yield {
        fail(s"result = $result, expected = $expected")
      }
      assertThrows[ExpectedException] {
        io.unsafeRunSync()
      }
    }
    "not short circuit if both aye" in {
      val io = for {
        result    <- aye *> aye
        expected  <- aye
      } yield {
        println(s"result = $result, expected = $expected")
        result shouldBe expected
      }
      io.unsafeRunSync()
    }
  }

  "Monads" should {
    type MonadType = Either[String, Int]
    val aye: MonadType = Right(1)
    val nay: MonadType = Left("nope")
    "short-circuit" in {
      aye *> nay shouldBe nay
    }
    "not short circuit" in {
      aye *> aye shouldBe aye
    }
  }

}
