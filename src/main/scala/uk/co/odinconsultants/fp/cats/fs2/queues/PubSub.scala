package uk.co.odinconsultants.fp.cats.fs2.queues

import java.util.concurrent.TimeUnit

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.syntax.all._
import fs2.concurrent.{SignallingRef, Topic}
import fs2.{Pipe, Stream}

import scala.concurrent.duration._
import scala.language.higherKinds


/**
 * @see https://fs2.io/concurrency-primitives.html
 */
sealed trait Event
case class Text(value: String) extends Event
case object Quit extends Event

class EventService[F[_]](eventsTopic: Topic[F, Event], interrupter: SignallingRef[F, Boolean])(
  implicit F: Concurrent[F],
  timer: Timer[F]
) {

  // Publishing 15 text events, then single Quit event, and still publishing text events
  def startPublisher: Stream[F, Unit] = {
    val textEvents = eventsTopic.publish(
      Stream.awakeEvery[F](1.second)
        .zipRight(Stream.eval(timer.clock.realTime(TimeUnit.MILLISECONDS).map(t => Text(t.toString))).repeat)
    )

    val quitEvent = Stream.eval(eventsTopic.publish1(Quit))

    (textEvents.take(15) ++ quitEvent ++ textEvents).interruptWhen(interrupter)
  }

  // Creating 3 subscribers in a different period of time and join them to run concurrently
  def startSubscribers: Stream[F, Unit] = {
    def processEvent(subscriberNumber: Int): Pipe[F, Event, Unit] =
      _.flatMap {
        case e @ Text(_) =>
          Stream.eval(F.delay(println(s"Subscriber #$subscriberNumber processing event: $e")))
        case Quit => Stream.eval(F.delay{println(s"Quit event for #$subscriberNumber")}) ++ Stream.eval(interrupter.set(true))
      }

    val events: Stream[F, Event] =
      eventsTopic.subscribe(10)

    Stream(
      events.through(processEvent(1)),
      events.delayBy(5.second).through(processEvent(2)),
      events.delayBy(10.second).through(processEvent(3))
    ).parJoin(3)
  }
}

object PubSub {

  def program(implicit c: Concurrent[IO], t: Timer[IO]): Stream[IO, Unit] = for {
    topic   <- Stream.eval(Topic[IO, Event](Text("Initial Event")))
    signal  <- Stream.eval(SignallingRef[IO, Boolean](false))
    service = new EventService[IO](topic, signal)
    _       <- service.startPublisher.concurrently(service.startSubscribers)
  } yield ()

}

object PubSubMain extends IOApp {

  import PubSub._

  override def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)
}
