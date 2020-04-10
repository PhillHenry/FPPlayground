package uk.co.odinconsultants.fp.zio.applicatives

import org.scalatest.{Assertion, Matchers, WordSpec}
import zio.{IO, UIO, ZIO}

/**
 * Deliberately avoiding the excellent ZIO testing framework to see what ZIO's doing under the covers.
 */
class ApplicativeSpec extends WordSpec with Matchers {

  "Applicatives" should {
    val nay: IO[Int, Nothing] = ZIO.fail(-1)
    val aye: UIO[Int]         = ZIO.succeed(1)
    "yield failure if (success x failure)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      zio.Runtime.default.unsafeRunSync(result)
    }
    "yield failure if (failure x success)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- nay *> aye
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      zio.Runtime.default.unsafeRunSync(result)
    }
    "yield success if (success x success)" in {
      val result: ZIO[Any, Nothing, Int] = for {
        actual    <- aye *> aye
        expected  <- aye
      } yield {
        println(s"actual = $actual, expected = $expected")
        actual shouldBe 1
        1
      }
      zio.Runtime.default.unsafeRunSync(result)
    }
  }

}
