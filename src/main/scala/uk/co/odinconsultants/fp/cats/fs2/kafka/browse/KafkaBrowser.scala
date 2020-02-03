package uk.co.odinconsultants.fp.cats.fs2.kafka.browse

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, _}
import uk.co.odinconsultants.fp.cats.fs2.example.ConsumerKafka.forEachPartition

object KafkaBrowser extends IOApp {

  type Key                          = String
  type Value                        = String
  type MyKafkaConsumer              = KafkaConsumer[IO, Key, Value]
  type MyCommittableConsumerRecord  = CommittableConsumerRecord[IO, Key, Value]
  type PartitionStreams             = Stream[IO, Stream[IO, MyCommittableConsumerRecord]]

  val byteDeserializer: Deserializer[IO, String] = Deserializer.lift(bytes => IO.pure(if (bytes == null) "" else new String(bytes.dropWhile(_ == 0))))

  def consumerSettings(host: String, port: Int): ConsumerSettings[IO, String, String] =
    ConsumerSettings[IO, Key, Value](
      keyDeserializer = byteDeserializer,
      valueDeserializer = byteDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(s"$host:$port")
      .withGroupId("TODO") // without => "To use the group management or offset commit APIs, you must provide a valid group.id in the consumer configuration."

  override def run(args: List[String]): IO[ExitCode] = {
    val host: String      = args.head
    val port: Int         = args(1).toInt
    val topicName: String = args(2)
    val offset: Long      = args(3).toLong

    readFrom(host, port, topicName, offset).take(3).compile.drain.map(_ => ExitCode.Success)
  }

  private def readFrom(host:      String,
                       port:      Int,
                       topicName: String,
                       offset:    Long): Stream[IO, Unit] = {
    val consumer: Stream[IO, MyKafkaConsumer] = consumerStream[IO].using(consumerSettings(host, port))

    val subscribeFn: MyKafkaConsumer => IO[Unit] = _.subscribeTo(topicName)


    val partitionStreamsFn: MyKafkaConsumer => PartitionStreams = { c =>
      c.partitionedStream
    }

    val printOut: MyCommittableConsumerRecord => IO[Unit] = { committable =>
      IO {
        println(s"committable = $committable")
      }
    }



    pipeline(consumer, subscribeFn, printOut, partitionStreamsFn)
  }

  /**
   * @tparam K KafkaConsumer
   * @tparam C CommittableConsumerRecord
   * @tparam P Output
   */
  def pipeline[K, C, P](s:                  Stream[IO, K],
                        subscribe:          K => IO[Unit],
                        recordAction:       C => IO[P],
                        partitionStreamsFn: K => Stream[IO, Stream[IO, C]]): Stream[IO, P] = {
    s.evalTap(subscribe).flatMap(partitionStreamsFn).flatMap { partitionStream =>
      forEachPartition(recordAction, partitionStream)
    }
  }


  def forEachPartition[T, C](action: C => IO[T], s: Stream[IO, C]): Stream[IO, T] =
    s.flatMap { c => Stream.eval(action(c)) }
}
