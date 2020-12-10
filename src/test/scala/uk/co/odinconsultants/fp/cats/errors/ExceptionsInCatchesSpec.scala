package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{IO, Resource}
import org.scalatest.{Matchers, WordSpec}

class ExceptionsInCatchesSpec extends WordSpec with Matchers {

  val input                                     = 1
  val happyPathIO:            IO[Int]           = IO.pure(input)
  val releaseBarfs:           Resource[IO, Int] = resourceBlowsUpOnRelease(happyPathIO)
  val makeAndReleaseBarfs:    Resource[IO, Int] = resourceBlowsUpOnRelease(IO { throw new Exception(makeErrorMessage)})
  def unhappyPathIO(x: Int):  IO[Int]           = IO { throw new Exception(useErrorMessage(x))}

  "'make' and 'use' succeeds but 'release' barfs" should {
    "give the 'use' error and suppress the 'release' error" in {
      val caught: Exception = useSucceedsButExpectErrorWhen(releaseBarfs)
      caught.getMessage shouldEqual releaseErrorMessage(input)
    }
  }

  "'use' succeeds but 'release' and 'make' barf" should {
    "give the 'use' error and suppress the 'release' error" in {
      val caught: Exception = useSucceedsButExpectErrorWhen(makeAndReleaseBarfs)
      caught.getMessage shouldEqual makeErrorMessage
    }
  }

  "'make' succeeds but 'use' and 'release' failing" should {
    "give the 'use' error and suppress the 'release' error" in {
      val caught: Exception = expectUseErrorMessageWhen(releaseBarfs)
      caught.getMessage shouldEqual useErrorMessage(input)
    }
  }

  "'use' and 'make' and 'release' are all failing" should {
    "give the 'use' error and suppress the 'release' error" in {
      val caught: Exception = expectUseErrorMessageWhen(makeAndReleaseBarfs)
      caught.getMessage shouldEqual makeErrorMessage
    }
  }

  private def useSucceedsButExpectErrorWhen(resource: Resource[IO, Int]) = {
    val caught = intercept[Exception] {
      resource.use(x => IO {
        println(s"Success with $x")
      }).unsafeRunSync()
    }
    caught.getSuppressed should be(empty)
    caught
  }

  def expectUseErrorMessageWhen(resource: Resource[IO, Int]): Exception = {
    val caught = intercept[Exception] {
      resource.use(unhappyPathIO).unsafeRunSync()
    }
    caught.getSuppressed should be(empty)
    caught
  }

  private def resourceBlowsUpOnRelease(inputIO: IO[Int]): Resource[IO, Int] =
    Resource.make(inputIO)(x => IO {
      throw new Exception(releaseErrorMessage(x))
    })

  private def useErrorMessage(x: Int) = s"Use failure with $x"

  private def releaseErrorMessage(x: Int) = s"release failing for $x"

  private val makeErrorMessage = s"make failing"
}
