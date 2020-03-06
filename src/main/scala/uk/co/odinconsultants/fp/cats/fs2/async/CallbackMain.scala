package uk.co.odinconsultants.fp.cats.fs2.async

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

/**


Jakub Kozłowski @kubukoz Feb 29 20:14
I suppose it's just outright wrong (the callback is wrapped in one that trampolines the call) - still not sure why the execution of later blocks happens before that finished $i call, but I'll get to it

Jakub Kozłowski @kubukoz Feb 29 21:09
oh, I see. The trampolined EC only runs the next steps of the IO immediately (during cb) if it's not in a cb itself. Otherwise, it queues that for later

Jakub Kozłowski @kubukoz Feb 29 21:17
the new intuition I have is that cb is "(up to) the rest of the continuation until an async boundary or another async" - "up to", because if you're already in such a continuation you'll only queue up your task until the block passed to async { cb => ... } completes
does that make sense?
 */
object CallbackMain extends IOApp {
  def block(i: Int) =
    IO.async[Unit] { cb =>
      println(s"before $i")
      cb(Right(()))
      println(s"finished $i")
    }

  def run(args: List[String]): IO[ExitCode] = {
    block(1) *>
      IO(println("between 1/2")) *>
      block(2) *>
      IO(println("between 2/3")) *>
      block(3)
    }.as(ExitCode.Success)
}
