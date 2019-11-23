package uk.co.odinconsultants.fp.cats.fs2.example

import java.util.concurrent.atomic.AtomicInteger

import cats.effect.IO
import cats.effect.concurrent.Ref
import org.scalatest.{Matchers, WordSpec}
import fs2.{Pipe, Stream}

class ConsumerMainSpec extends WordSpec with Matchers {

  import ConsumerMain._

  case class MockKafka()
  case class MockRecord(id: Int)
  case class MockProducerRecords(id: Int)
  case class MockCommittableOffset(id: Int)

  "Kafka pipeline" should {
    "Read, write and commit" in {
      /*
      "StateT is not safe to use with effect types, because it's not safe in the face of concurrent access.
      Instead, consider using a Ref (from either fs2 or cats-effect, depending what version)."
      https://stackoverflow.com/questions/51624763/fs2-stream-with-statetio-periodically-dumping-state
       */
      val nToRead = 10
      val nReadCommitted = Ref[IO].of(0)
      val nWriteCommitted = Ref[IO].of(0)

      val s = Stream.emit(MockKafka()).covary[IO]

      val subscribe: MockKafka => IO[Unit] =
        _ => IO {
          println("subscribed")
        }

      val toRecords: MockKafka => Stream[IO, MockRecord] =
        _ => Stream.emits((1 to nToRead).map(x => MockRecord(x))).covary[IO]

      val producerPipe: Pipe[IO, MockProducerRecords, MockRecord] =
        s => s.map(p => MockRecord(p.id))

      val toWriteRecords: MockRecord => MockCommittableOffset =
        r => MockCommittableOffset(r.id)

      for {
        readState   <- Stream.eval(nReadCommitted)
        writeState  <- Stream.eval(nWriteCommitted)
      } yield {

        val commitRead: MockRecord => IO[MockProducerRecords] = r => readState.update(_ + 1).flatMap( _ => IO { MockProducerRecords(r.id) } )

//        val commitWrite: Pipe[IO, MockCommittableOffset, Unit] = s => s.map(_ => writeState.update(_ + 1))
        val commitWrite: Pipe[IO, MockCommittableOffset, Unit] =
          s => s.map(o => println(s"commitWrite $o"))

        val x = pipeline(s, subscribe, toRecords, commitRead, producerPipe, toWriteRecords, commitWrite)

        x.append {
          val assertion: IO[Unit] = makeAssertion(nToRead, readState)
          Stream.eval(assertion)
        }
      }.compile.drain.unsafeRunSync()

    }
  }

  private def makeAssertion(expected: Int, state: Ref[IO, Int]): IO[Unit] =
    state.get.flatMap(x => IO {
      x shouldBe expected
      println(s"x = $x")
    })
}
