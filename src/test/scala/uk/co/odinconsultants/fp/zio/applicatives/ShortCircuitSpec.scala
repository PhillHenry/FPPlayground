package uk.co.odinconsultants.fp.zio.applicatives

import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{Assertion, Matchers, WordSpec}
import zio.{IO, UIO, ZIO}

/**
 * Deliberately avoiding the excellent ZIO testing framework to see what ZIO's doing under the covers.
 */
class ShortCircuitSpec extends WordSpec with Matchers {


  trait ShortCircuitFixture {
    var nCalls = new AtomicInteger(0)
    val nay: IO[Int, Nothing] = ZIO.fail {
      println("fail")
      nCalls.incrementAndGet()
      -1
    }
    val aye: UIO[Int]         = ZIO.succeed {
      println("success")
      nCalls.incrementAndGet()
      1
    }
  }
  val zioRuntime: zio.Runtime[zio.ZEnv] = zio.Runtime.default

  /* adamfraserToday at 11:22 AM
@PhillHenry Take a look at ZIO#validate.*/

  /*
   * jdegoes 12 April 2020 at 2:10 PM
   * @PhillHenry x1.ignore &> x2.ignore &> x3. This will execute x1, x2, and x3 in parallel, using zipRightPar
   *            (that's the &> operator, zips two effects together in parallel, returning whatever is produced on the right),
   *            and ignore the result of x1 and x2 so their failures don't influence the result of the computation.
   * @PhillHenry In ZIO, even parallel zip or collect or foreach operations will "kill" the other running effects if
   *            one of them fails. Because that's often what you want. To get the other behavior, just use .ignore in
   *            the right places to ignore the failures you don't care about.
   */
  "Using &>" should {
    "not short circuit" in new ShortCircuitFixture {
      val xs: ZIO[Any, Throwable, Unit] = aye.ignore &> nay.ignore &> aye.ignore
      val exit = zioRuntime.unsafeRunSync(xs)
      exit.succeeded shouldBe true
      nCalls.get() shouldBe 3
    }
  }

  "Using collectAll" should {
    "not short-circuit if we use .ignore" in new ShortCircuitFixture {
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
    "yield failure if (success x failure)" in new ShortCircuitFixture {
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
    "yield failure if (success x failure x success x failure)" in new ShortCircuitFixture {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- aye *> nay *> aye *> nay
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      val exit = zioRuntime.unsafeRunSync(result) // note: short circuits
      exit.succeeded shouldBe false
    }
    "yield failure if (failure x success)" in new ShortCircuitFixture {
      val result: ZIO[Any, Int, Nothing] = for {
        actual    <- nay *> aye
        expected  <- nay
      } yield {
        fail(s"actual = $actual, expected = $expected")
      }
      val exit = zioRuntime.unsafeRunSync(result) // note: short circuits
      exit.succeeded shouldBe false
    }
    "yield success if (success x success)" in new ShortCircuitFixture {
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
