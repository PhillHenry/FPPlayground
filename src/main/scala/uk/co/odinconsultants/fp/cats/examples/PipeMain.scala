package uk.co.odinconsultants.fp.cats.examples

import java.io.{InputStream, OutputStream, PipedInputStream, PipedOutputStream}

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Resource}

import scala.concurrent.ExecutionContext

object PipeMain extends IOApp {
  def fromAutoCloseable[A <: AutoCloseable](acquire: => A): Resource[IO, A] = {
    Resource.fromAutoCloseable { IO.delay { acquire } }
  }

  def processInputStream(inputStream: InputStream, temp: OutputStream): IO[Unit] = IO {
//    temp.write(inputStream.readAllBytes())
    println("This ^ is a Java 11 method.")
  }


  override def run(args: List[String]): IO[ExitCode] = {

    val inputOutput: Resource[IO, (InputStream, OutputStream)] = for {
              in <- fromAutoCloseable { new PipedInputStream() }
              out <- fromAutoCloseable { new PipedOutputStream(in) }
    } yield (in, out)


    val processed: Resource[IO, InputStream] = inputOutput.evalMap { case (in, out) =>

      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

      processInputStream(in, out).start(contextShift).handleErrorWith { exception: Throwable =>
        println(exception.getMessage)
        IO.raiseError(exception)
      }.map { signatureDataFiber =>
        in
      }
    }

    processed.use { is =>
      IO(is.read())
    }.map(_ => ExitCode.Success)
  }
}
