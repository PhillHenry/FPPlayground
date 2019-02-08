package uk.co.odinconsultants.fp.cats.io

import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @see https://typelevel.org/cats-effect/datatypes/io.html#asynchronous-effects--ioasync--iocancelable
  */
object SyncAndCancellable {

  def convert[A](fa: => Future[A])(implicit ec: ExecutionContext): IO[A] =
    IO.async { cb =>
      // This triggers evaluation of the by-name param and of onComplete,
      // so it's OK to have side effects in this callback
      fa.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }

  def main(args: Array[String]): Unit = {
    import ExecutionContext.Implicits._
    val io = convert(Future {
      println("my future")
      1
    })
    println(io)

    // "my future"
    // 1
    println(io.unsafeRunSync())

    // "my future"
    // 1
    println(io.unsafeRunSync())

  }

}
