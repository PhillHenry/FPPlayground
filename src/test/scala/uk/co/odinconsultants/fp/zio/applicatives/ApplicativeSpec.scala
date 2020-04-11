package uk.co.odinconsultants.fp.zio.applicatives

import org.scalatest.{Assertion, Matchers, WordSpec}
import zio.{IO, UIO, ZIO}

/**
 * Deliberately avoiding the excellent ZIO testing framework to see what ZIO's doing under the covers.
 */
class ApplicativeSpec extends WordSpec with Matchers {

  "Applicatives" should {
    val nay: IO[Int, Nothing] = ZIO.fail {
      println("fail")
      -1
    }
    val aye: UIO[Int]         = ZIO.succeed {
      println("success")
      1
    }
    val zioRuntime: zio.Runtime[zio.ZEnv] = zio.Runtime.default
    "yield failure if (success x failure)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      zioRuntime.unsafeRunSync(result)
    }
    "yield failure if (success x failure x success x failure)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- aye *> nay *> aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      zioRuntime.unsafeRunSync(result) // note: short circuits
    }
    "yield failure if (failure x success)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- nay *> aye
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      zioRuntime.unsafeRunSync(result)
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
      zioRuntime.unsafeRunSync(result)
    }
  }

}
