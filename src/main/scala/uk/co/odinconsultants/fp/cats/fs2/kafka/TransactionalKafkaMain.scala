package uk.co.odinconsultants.fp.cats.fs2.kafka

import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.kafka._
import fs2.{Chunk, Pipe, Stream}
import uk.co.odinconsultants.fp.cats.fs2.example.Settings._

object TransactionalKafkaMain extends IOApp {

  val producerSettings: TransactionalProducerSettings[IO, String, String] =
    TransactionalProducerSettings(
      "transactional-id",
      ProducerSettings[IO, String, String]
        .withBootstrapServers(s"""localhost:$port""")
    )

  override def run(args: List[String]): IO[ExitCode] = {
    val pStream: Resource[IO, TransactionalKafkaProducer[IO, String, String]] = transactionalProducerResource[IO].using(producerSettings)
//    pStream.flatMap { producer =>
//      val records: CommittableProducerRecords[IO, String, String] = CommittableProducerRecords.apply(IO { ProducerRecord(topicName, "key", "value") } )
//      val chunk: Chunk[CommittableProducerRecords[IO, String, String]] = Chunk(records)
//      producer.produce(TransactionalProducerRecords.apply(chunk))
//      ???
//    }

      val sMsgs = Stream.range(0, 5, 1).covary[IO].map { x => ProducerRecords.one(ProducerRecord(topicName, s"${x}Key", s"${x}Value")) }

//    s.compile.drain.as(ExitCode.Success)
    ???
  }
}
