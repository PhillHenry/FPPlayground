package uk.co.odinconsultants.fp.cats.fs2.example

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import fs2.{Stream, Pipe}

class ConsumerMainSpec extends WordSpec with Matchers {

  import ConsumerMain._

  case class MockKafka()
  case class MockRecord(id: Int)
  case class MockProducerRecords(id: Int)
  case class MockCommittableOffset(id: Int)

  "Kafka pipeline" should {
    "Read, write and commit" in {
      val nRead = 10

      val subscribe: MockKafka => IO[Unit] =
        _ => IO { println("subscribed") }

      val toRecords: MockKafka => Stream[IO, MockRecord] =
        _ => Stream.emits((1 to nRead).map(x => MockRecord(x))).covary[IO]

      val commitRead: MockRecord => IO[MockProducerRecords] =
        r => IO { println(s"commiting $r") ; MockProducerRecords(r.id) }

      val producerPipe: Pipe[IO, MockProducerRecords, MockRecord] =
        s => s.map(p => MockRecord(p.id))

      val toWriteRecords: MockRecord => MockCommittableOffset =
        r => MockCommittableOffset(r.id)

      val commitWrite: Pipe[IO, MockCommittableOffset, Unit] =
        s => s.map(o => println(s"commitWrite $o"))

      val x = pipeline(Stream.emit(MockKafka()).covary[IO], subscribe, toRecords, commitRead, producerPipe, toWriteRecords, commitWrite)

      x.compile.drain.unsafeRunSync()
      // TODO some assertions
    }
  }

}
