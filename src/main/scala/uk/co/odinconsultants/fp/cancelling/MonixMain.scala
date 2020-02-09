package uk.co.odinconsultants.fp.cancelling

import cats.effect.ExitCase.{Canceled, Completed, Error}
import cats.effect.{ExitCode, IO, IOApp, Timer}
import monix.eval.Task

import scala.concurrent.duration._
import cats.implicits._
import monix.execution.Scheduler

import scala.concurrent.ExecutionContext.Implicits.global

object MonixMain {

  private implicit val ioTimer: Timer[IO] = IO.timer(global)
  private implicit val scheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  def main(args: Array[String]): Unit = {
    val work = (
      Task.sleep(5.seconds) *>
        Task(println("Halfway")) *>
        Task.sleep(5.seconds) *>
        Task(println("Done"))
      ).bracket(_ => Task.unit)(_ => Task.unit)

    val toRun = (Task.race(work, (Task.sleep(4.seconds) *> Task(println("Finished")))))
      .guaranteeCase {
        case Completed => Task(println("Completed"))
        case Canceled  => Task(println("Cancelled"))
        case Error(_)  => Task(println("Error"))
      } *> ExitCode.Success.pure[Task]

    toRun.runSyncUnsafe(1 minute)
  }
}
