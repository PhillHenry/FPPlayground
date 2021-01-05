package uk.co.odinconsultants.fp.zio.errors
import zio.{Cause, Task, UIO, URIO, ZIO}

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

  class DomainException extends Exception

  val resourceTask: Task[PathologicalResource] = ZIO {
    pathologicalResource()
  }

  private def pathologicalResource(): PathologicalResource = {
    println("resource creation")
    new PathologicalResource
  }

  val domainUIO: UIO[PathologicalResource] = UIO {
    pathologicalResource()
  }

  val failRelease: ZIO[Any, Nothing, String] = domainUIO.bracket(closeZIO) { _ =>
    printAndReturn(acquireMessage)
  }

  override def run(args: List[String]) = {
    val app: ZIO[Any, DomainX, String] = failRelease // blows up with "Fiber failed." if not handled and the final message is not printed
    //    val app: ZIO[Any, Throwable, String] = ZIO { throw new Exception("boom") } // exception caught and polite message printed
    //    val x = app.catchAll(e => UIO { e.printStackTrace() })

    val x: ZIO[Any, Nothing, String] = handlePathogen(app)

    x.flatMap { msg => printAndReturn(s"finished politely w/: $msg") }.map(_ => 0)
  }

  type DomainX = DomainException

  def handlePathogen(app: ZIO[Any, DomainX, String]): ZIO[Any, Nothing, String] = {
    val sandboxed: ZIO[Any, Cause[DomainX], String] = app.sandbox
    sandboxed.either.map {
      case Left(oops)     => s"Oops: $oops"
      case Right(result)  => result
    }
  }
}
