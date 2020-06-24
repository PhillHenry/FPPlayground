package uk.co.odinconsultants.fp.cats.errors

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}

class MyExceptionHandling extends WordSpec with Matchers {

  case class MyDomain(x: Int, y: String)

  "Monad" should {
    val x                         = new Exception()
    val io:         IO[Nothing]   = IO.raiseError(x)
    val massagedIO: IO[MyDomain]  = io
    val result:     IO[Int]       = for {
      err <- massagedIO
    } yield {
      println(err) // but of course, we never get this far
      err.x
    }
    "is fine if you attempt an IO that has raised an exception" in {
      result.attempt.unsafeRunSync() shouldBe Left(x)
    }
    "blows up if you don't attempt it" ignore { // ignored because it leads to "java.lang.Exception was thrown."
      result.unsafeRunSync() shouldBe Left(x)
    }
  }

}
