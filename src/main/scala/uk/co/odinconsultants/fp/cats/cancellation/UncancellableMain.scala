package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.concurrent.Deferred
import cats.effect.{ExitCase, ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.duration._

/**

Isn't notCancelable.uncancelable what you're looking for then?

Andrey Bobylev @XoJIoD89 Mar 01 15:48
AFAIK cancelation hits in async boundaries and flatMap calls

docs state that cancellation boundary is inserted every 512 flatMaps AFTER an async boundary


Gabriel Volpe @gvolpe Mar 01 15:48
Yeah, that is called fairness
Cats IO and Monix do it differently
 */
object UncancellableMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val x: IO[Unit] = (IO(println("foo")) *> IO.never).start.uncancelable.flatMap(_.cancel)
    x.map(_ => ExitCode.Success) // always prints 'foo' for me
  }
}
