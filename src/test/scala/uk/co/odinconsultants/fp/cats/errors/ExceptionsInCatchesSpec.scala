package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{IO, Resource}
import org.scalatest.{Matchers, WordSpec}

class ExceptionsInCatchesSpec extends WordSpec with Matchers {

  val input                           = 1
  val happyPathIO:  IO[Int]           = IO.pure(input)

  def unhappyPathIO(x: Int): IO[Int]  = IO { throw new Exception(useErrorMessage(x))}

  val releaseBarfs: Resource[IO, Int] = resourceBlowsUpOnRelease(happyPathIO)
  val makeBarfs:    Resource[IO, Int] = resourceBlowsUpOnRelease(unhappyPathIO(input))

  "Try succeeds but finally barfs" should {
    "be produce message in close" in {
      val caught = intercept[Exception] {
        releaseBarfs.use(x => IO { println(s"Success with $x")}).unsafeRunSync()
      }
      caught.getMessage shouldEqual releaseErrorMessage(input)
      caught.getSuppressed should be (empty)
    }
  }

  "Try fails and so does finally" should {

    def expectUseErrorMessageForUnsafe(resource: Resource[IO, Int]): Exception = {
      val caught = intercept[Exception] {
        resource.use(unhappyPathIO).unsafeRunSync()
      }
      caught.getMessage shouldEqual useErrorMessage(input)
      caught.getSuppressed should be(empty)
      caught
    }

    "give the 'use'' error abd suppress the 'close' error" in {
      expectUseErrorMessageForUnsafe(releaseBarfs)
    }
    "result in the same even with Resource.make being the unhappy path" in {
      expectUseErrorMessageForUnsafe(makeBarfs)
    }
  }


  private def resourceBlowsUpOnRelease(inputIO: IO[Int]): Resource[IO, Int] =
    Resource.make(inputIO)(x => IO {
      throw new Exception(releaseErrorMessage(x))
    })

  private def useErrorMessage(x: Int) = s"Use failure with $x"

  private def releaseErrorMessage(x: Int) = s"releasing $x"
}
