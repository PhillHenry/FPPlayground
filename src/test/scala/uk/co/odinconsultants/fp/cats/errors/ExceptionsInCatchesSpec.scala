package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{IO, Resource}
import org.scalatest.{Matchers, WordSpec}

class ExceptionsInCatchesSpec extends WordSpec with Matchers {


  val input                           = 1
  val happyPathIO:  IO[Int]           = IO.pure(input)

  "Try succeeds but finally barfs" should {
    val releaseBarfs: Resource[IO, Int] = resourceBlowsUpOnRelease(happyPathIO)

    "be produce message in close" in {
      val caught = intercept[Exception] {
        releaseBarfs.use(x => IO { println(s"Success with $x")}).unsafeRunSync()
      }
      caught.getMessage shouldEqual releaseErrorMessage(input)
    }
  }


  "Try fails and so does finally" should {
    val releaseBarfs: Resource[IO, Int] = resourceBlowsUpOnRelease(happyPathIO)
    "be managed" in {
      val caught = intercept[Exception] {
        releaseBarfs.use(x => IO { throw new Exception(useErrorMessage(x))}).unsafeRunSync()
      }
      caught.getMessage shouldEqual useErrorMessage(input)
    }
  }

  private def useErrorMessage(x: Int) = s"Use failure with $x"

  private def resourceBlowsUpOnRelease(inputIO: IO[Int]): Resource[IO, Int] =
    Resource.make(inputIO)(x => IO {
      throw new Exception(releaseErrorMessage(x))
    })

  private def releaseErrorMessage(x: Int) = s"releasing $x"
}
