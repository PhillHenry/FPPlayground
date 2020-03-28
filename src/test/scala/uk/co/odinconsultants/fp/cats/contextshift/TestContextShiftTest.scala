package uk.co.odinconsultants.fp.cats.contextshift

import cats.effect.laws.util.TestContext
import cats.effect.{ContextShift, IO, Timer}

import org.scalactic.source.Position
import org.scalatest.{Assertion, AsyncFunSpec, AsyncTestSuite, Matchers}
import org.scalatest.exceptions.TestFailedException

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
 * See https://gist.github.com/Daenyth/67575575b5c1acc1d6ea100aae05b3a9
 */
trait IOAssertions { self: AsyncTestSuite =>

  // It's "extremely unsafe" because it's an implicit conversion that takes a pure value
  // and silently converts it to a side effecting value that begins running on a thread immediately.
  implicit def extremelyUnsafeIOAssertionToFuture(ioa: IO[Assertion])(
    implicit pos: Position
  ): Future[Assertion] = {
    val _ = pos // unused here; exists for override to use
    ioa.unsafeToFuture()
  }

  implicit protected class IOAssertionOps[A](val io: IO[A])(
    implicit pos: Position
  ) {

    /** Same as shouldNotFail, but preferable in cases where explicit type signatures are not used,
     * as `shouldNotFail` will discard any result, and this method will only compile where the author
     * intends `io` to be made from assertions.
     *
     * @example {{{
     *   List(1,2,3,4,5,6).traverse { n =>
     *     databaseCheck(n).map { result =>
     *       result shouldEqual GoodValue
     *     }
     *   }.flattenAssertion
     * }}}
     * */
    def flattenAssertion(implicit ev: A <:< Seq[Assertion]): IO[Assertion] = {
      val _ = ev // "unused implicit" warning
      io.shouldNotFail
    }

    def shouldNotFail: IO[Assertion] = io.attempt.flatMap {
      case Left(failed: TestFailedException) =>
        IO.raiseError(failed)
      case Left(err) =>
        IO(fail(s"IO Failed with ${err.getMessage}", err)(pos))
      case Right(_) =>
        IO.pure(succeed)

    }

    /**
     * Equivalent to [[assertThrows]] for [[IO]]
     */
    def shouldFailWith[T <: AnyRef](
                                     implicit classTag: ClassTag[T]
                                   ): IO[Assertion] =
      io.attempt.flatMap { attempt =>
        IO(assertThrows[T](attempt.toTry.get))
      }
  }
}


/** Locates ContextShift[IO] and Timer[IO] via implicit ExecutionContext, similar to how cats-effect 0.10 worked. */
trait ContextShiftTest {

  implicit protected def CS(implicit ec: ExecutionContext): ContextShift[IO] =
    IO.contextShift(ec)

  implicit protected def timer(implicit ec: ExecutionContext): Timer[IO] =
    IO.timer(ec)
}

/** Overrides ContextShiftTest behaviors to be provided by cats-effect `TestContext` */
trait TestContextShiftTest extends ContextShiftTest with IOAssertions {
  this: AsyncTestSuite =>
  final protected val ctx = TestContext()

  implicit final override protected def CS(
                                            implicit ec: ExecutionContext
                                          ): ContextShift[IO] =
    ctx.contextShift[IO](IO.ioEffect)

  implicit final override protected def timer(
                                               implicit ec: ExecutionContext
                                             ): Timer[IO] = ctx.timer[IO]

  implicit final override def extremelyUnsafeIOAssertionToFuture(
                                                                  test: IO[Assertion]
                                                                )(implicit pos: Position): Future[Assertion] = {

    val result: Future[Assertion] = test.unsafeToFuture()
    ctx.tick(1000.day) // Advance the clock

    if (result.value.isDefined)
      result
    else {
      fail(
        s"""Test probably deadlocked. Test `IO` didn't resolve after simulating 1000 days of time.
           | Remaining tasks: ${ctx.state.tasks}""".stripMargin
      )(pos)
    }
  }
}

/** Spec base class making it easier to standardize IO-based tests */
trait IOSpec
  extends AsyncFunSpec with Matchers with IOAssertions with ContextShiftTest
