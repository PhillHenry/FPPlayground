package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import fs2.Stream
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext

class TipsForWorkingWithFS2Spec extends WordSpec with Matchers {

  import TipsForWorkingWithFS2._

  "FS2" should {
    "create a stream" in {
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val F                              = implicitly[ConcurrentEffect[F]]

      val stream: Stream[F,Row] = rows(h)

    }
  }

}
