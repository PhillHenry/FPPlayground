package uk.co.odinconsultants.fp.cats.errors

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import uk.co.odinconsultants.fp.cats.errors.ExceptionMain._

class ExceptionMainSpec extends WordSpec with Matchers {

  "Applicative.pure(throw new Exception..." should {
    "propagate the error" in {
      intercept[Exception] {
        foo[IO]().unsafeRunSync()
      }
    }
  }

  "IO { throw new Exception..." should {
    "propagate the error" in {
      intercept[Exception] {
        bar().unsafeRunSync()
      }
    }
  }

}
