package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{IO, IOApp}
import cats.effect.{ExitCode, IO, IOApp, Timer}
import scala.concurrent.duration._
import cats.implicits._

object MyRethrowMain extends IOApp {

  case class Table(x: String)

  val trigger = "blow up!"

  def loadTable(table: Table): IO[Table] = IO {
    if (table.x == trigger)
      throw new Exception(trigger)
    else
      Table(s"loaded ${table.x}")
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val tableList = List(Table("1"), Table(trigger), Table("2"), Table(trigger), Table("3"))

    val tableFunction: Table => IO[Unit] = x =>  IO(println(s"tapped $x"))

    val attemptFunction: Either[Throwable, Table] => IO[Unit] = _ match {
      case Left(x)  => IO(println(s"Blew up on tapping with $x"))
      case Right(x) => tableFunction(x)
    }

    val io: IO[Unit] = tableList.parTraverse_ { t =>
      val attempted:        IO[Either[Throwable, Table]] = loadTable(t).attempt
      val flatMapAttempted: IO[Either[Throwable, Table]] = attempted.flatTap(attemptFunction)
//      flatMapAttempted.rethrow // rethrow actually blows up the App!
//      val plainFlatMap: IO[Table] = loadTable(t).flatTap(tableFunction)
//      plainFlatMap // blows up the App!
      flatMapAttempted
    }
    // Gavin Bisesi @Daenyth Jun 05 18:06 (typelevel/cats-effect Gitter)
    //  ^ simple and straightforward
    //    the attempt/rethrow thing is almost .guaranteeCase except that guarantee lets you handle "cancelled" too
    //  whereas attempt/rethrow means that cancel also cancels the flatTap part
    (io *> IO { println("finished") }).as(ExitCode.Success)
  }
}
