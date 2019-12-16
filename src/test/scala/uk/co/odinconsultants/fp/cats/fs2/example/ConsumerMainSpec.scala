package uk.co.odinconsultants.fp.cats.fs2.example

import java.util.concurrent.atomic.AtomicInteger

import cats.effect.IO
import cats.effect.concurrent.Ref
import org.scalatest.{Matchers, WordSpec}
import fs2.{Pipe, Stream}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConsumerMainSpec extends WordSpec with Matchers {

  import ConsumerMain._

  case class Kafka()
  case class Record(id: Int)
  case class ProducerRecords(id: Int)
  case class CommittableOffset(id: Int)

  "Kafka pipeline" should {
    "Read, write and commit" in {
      /*
      "StateT is not safe to use with effect types, because it's not safe in the face of concurrent access.
      Instead, consider using a Ref (from either fs2 or cats-effect, depending what version)."
      https://stackoverflow.com/questions/51624763/fs2-stream-with-statetio-periodically-dumping-state
       */
      val nToRead         = 10
      val nReadCommitted  = Ref[IO].of(0)
      val nWriteCommitted = Ref[IO].of(0)

      val s = Stream.emit(Kafka()).covary[IO]

      val subscribe: Kafka => IO[Unit] =
        _ => IO {
          println("subscribed")
        }

      val records = (1 to nToRead).map(x => Record(x))

      val toRecords: Kafka => Stream[IO, Record] =
        _ => Stream.emits(records).covary[IO]

      val producerPipe: Pipe[IO, ProducerRecords, Record] =
        s => s.map(p => Record(p.id))

      val toWriteRecords: Record => CommittableOffset =
        r => CommittableOffset(r.id)

      import cats.implicits._
      Stream.eval {
        nReadCommitted product nWriteCommitted
      }.flatMap { case (readState, writeState) =>

        val commitRead: Record => IO[ProducerRecords] = r => readState.update(_ + 1).flatMap(_ => IO {
          ProducerRecords(r.id)
        })

        // final type (T) is irrelevant but we do need a flatMap
        val commitWrite: Pipe[IO, CommittableOffset, Unit] = s => s.flatMap(c => Stream.eval(writeState.update(_ + 1)))

        val x = pipeline(s, subscribe, toRecords, commitRead, producerPipe, toWriteRecords, commitWrite)

        val assertN = makeAssertion(nToRead) _
        x ++ Stream.eval(assertN(readState) *> assertN(writeState))
      }.compile.drain.unsafeRunSync()
    }
  }

  private def makeAssertion(expected: Int)(state: Ref[IO, Int]): IO[Unit] =
    state.get.flatMap(x => IO {
      x shouldBe expected
      println(s"x = $x")
    })
}
