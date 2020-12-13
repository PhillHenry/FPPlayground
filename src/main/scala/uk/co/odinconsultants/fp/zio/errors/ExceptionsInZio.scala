package uk.co.odinconsultants.fp.zio.errors
import zio.{Task, UIO, URIO, ZIO}

object ExceptionsInZio extends zio.App {

  def printAndReturn(x: String): ZIO[Any, Nothing, String] = ZIO.succeed {
    println(x)
    x
  }

  val acquireMessage  = "acquire"

  val closeMessage    = "close"

  val closeException = new Exception(closeMessage)

  class PathologicalResource extends AutoCloseable {
    override def close(): Unit = throw closeException
  }

  val closeZIO: AutoCloseable => URIO[Any, Any] = x => URIO { x.close() }

  val failRelease: ZIO[Any, Throwable, String] = ZIO {
    println("resource creation")
    new PathologicalResource
  }.bracket(closeZIO) { _ =>
    printAndReturn(acquireMessage)
  }

  override def run(args: List[String]) = {
    val app: ZIO[Any, Throwable, String] = failRelease // blows up with "Fiber failed." and the final message is not printed
    //    val app: ZIO[Any, Throwable, String] = ZIO { throw new Exception("boom") } // exception caught and polite message printed
    //    val x = app.catchAll(e => UIO { e.printStackTrace() })

    val x = app.sandbox.either.map {
      case Left(oops) => s"Oops: $oops"
      case Right(result) => result
    }

    x.flatMap { msg => printAndReturn(s"finished politely w/: $msg") }.map(_ => 0)
  }
}
