package uk.co.odinconsultants.fp.zio.applicatives

import org.scalatest.{Assertion, Matchers, WordSpec}
import zio.{IO, UIO, ZIO}

/**
 * Deliberately avoiding the excellent ZIO testing framework to see what ZIO's doing under the covers.
 */
class ShortCircuitSpec extends WordSpec with Matchers {

  val nay: IO[Int, Nothing] = ZIO.fail {
    println("fail")
    -1
  }
  val aye: UIO[Int]         = ZIO.succeed {
    println("success")
    1
  }
  val zioRuntime: zio.Runtime[zio.ZEnv] = zio.Runtime.default

  "Using collectAll" should {
    "not short-circuit" in {
      // see https://github.com/zio/zio/issues/783 - collectAll => sequence in Cats land
      val sequenced: IO[Nothing, List[Unit]] = IO.collectAll(List(aye, nay, aye).map(_.ignore))
      val exit = zioRuntime.unsafeRunSync(sequenced)
      exit.map { xs =>
        println(s"xs = ${xs.mkString(", ")}")
        xs should have length 3
      }
    }
  }

  "Monads" should {
    "yield failure if (success x failure)" in {
      val result: ZIO[Any, Int, Int] = for {
        actual    <- aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
        actual
      }
      val exit = zioRuntime.unsafeRunSync(result)
      exit.succeeded shouldBe false
    }
    "yield failure if (success x failure x success x failure)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- aye *> nay *> aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      val exit = zioRuntime.unsafeRunSync(result) // note: short circuits
      exit.succeeded shouldBe false
    }
    "yield failure if (failure x success)" in {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- nay *> aye
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      val exit = zioRuntime.unsafeRunSync(result) // note: short circuits
      exit.succeeded shouldBe false
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
      val exit = zioRuntime.unsafeRunSync(result)
      exit.succeeded shouldBe true
    }
  }

}
