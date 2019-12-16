package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO}
import fs2.Stream
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext

//@RunWith(classOf[JUnitRunner])
class TipsForWorkingWithFS2Spec extends WordSpec with Matchers {

  import TipsForWorkingWithFS2._

  "FS2" should {
    "create a stream" in {
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val F                              = implicitly[ConcurrentEffect[F]]

      type O                    = Unit
      val fn: Row => IO[O]      = x => IO { println(s"PH: IO[$x]") }
      val stream: Stream[F,Row] = rows(h)
      val g                     = stream.evalMap(fn).compile
        .drain
//        .as(ExitCode.Success)

      def handle(e: Either[Throwable, O]): IO[O] = {
        IO { println("Finished")}
      }

//      g.runAsync(handle).unsafeRunSync()
      g.unsafeRunSync()
    }
  }

}
