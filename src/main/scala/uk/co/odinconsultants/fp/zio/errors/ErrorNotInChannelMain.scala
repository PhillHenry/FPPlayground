package uk.co.odinconsultants.fp.zio.errors

import java.io

import zio._

object ErrorNotInChannelMain extends App {

  case class MyError(msg: String)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
//    (withExpectedErrorType *> throwingException).catchAll(logError).as(0) // "test"
    val combined: ZIO[Any, java.io.Serializable, Nothing] = throwingException *> withExpectedErrorType
    combined.catchAll(logError).as(0)   // "java.lang.Exception: where does this go to?"
  }

  def logError(x: Any): UIO[Unit] = UIO(println(x))

  def withExpectedErrorType: IO[MyError, Nothing] = ZIO.fail(MyError("test"))

  def throwingException: Task[Nothing] = ZIO {
    throw new Exception("where does this go to?")
  }

  /**
  "Fiber failed.
  An unchecked error was produced."
   */
  def evil: Task[Nothing] = UIO {
    throw new Exception("where does this go to?")
  }

}
