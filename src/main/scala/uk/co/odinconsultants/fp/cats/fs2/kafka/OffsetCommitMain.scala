package uk.co.odinconsultants.fp.cats.fs2.kafka

import cats.{Applicative, FlatMap, Functor, ~>}
import cats.data.StateT
import cats.effect.{ExitCase, ExitCode, IO, IOApp}
import fs2.kafka.{CommittableConsumerRecord, CommittableOffsetBatch}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger

/**
 * Ryan Peters: The code stores the latest offset from Kafka, and commits it on finalize
 * Okay maybe it doesn't actually work. Effects run in my base F, but I can't seem to be able to update state. It's always empty (my default value)
 * Is this a known quirk/gotcha for using StateT with FS2?
 *
 * Ryan Peters @sloshy Mar 03 23:11
 * So I've explored the issue a bit more, I think what's going on is the first translate call is resetting the state for every element.
 *
 * Ryan Peters @sloshy Mar 03 23:37
 * Actually the second translate call is what's doing it... Maybe Ref/var actually is the best thing to do here
 */
object OffsetCommitMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    IO(ExitCode.Success)
  }


//  def doSomething[F[Tuple2]: Applicative: FlatMap: Functor: Logger, K, A](implicit ev: Applicative[Tuple1]) = { // I added the type classes. May very well be wrong.
//
//    val stream: Stream[F, CommittableConsumerRecord[F, K, Option[A]]] = ???
//
//    val fToState: F ~> StateT[F, CommittableOffsetBatch[F], *] =
//      StateT.liftK[F, CommittableOffsetBatch[F]]
//
////    val stateToF: StateT[F, CommittableOffsetBatch[F], *] ~> F =
////      λ[StateT[F, CommittableOffsetBatch[F], *] ~> F](_.run(CommittableOffsetBatch.empty[F]).map(_._2))
//
//    Stream
//      .force(stream)
//      .translate(fToState)
//      .evalTap(c => StateT.modify[F, CommittableOffsetBatch[F]](_.updated(c.offset)))
//      .map(_.record.value)
//      .unNone
//      .onFinalizeCaseWeak[StateT[F, CommittableOffsetBatch[F], *]] {
//        case ExitCase.Completed => StateT.get[F, CommittableOffsetBatch[F]].flatMapF(_.commit)
//        case ExitCase.Error(e)  => StateT.liftF(Logger[F].error(e)("Error occurred during result stream"))
//        case _                  => Applicative[StateT[F, CommittableOffsetBatch[F], *]].unit
//      }
////      .translate(stateToF)
//  }
}
