package uk.co.odinconsultants.fp.cats.fs2.kafka.browse

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{INothing, Stream}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, _}
import org.apache.kafka.common.TopicPartition
import uk.co.odinconsultants.fp.cats.fs2.example.ConsumerKafka.forEachPartition

import scala.collection.immutable.SortedSet

object KafkaBrowser extends IOApp {

  type Key                          = String
  type Value                        = String
  type MyKafkaConsumer              = KafkaConsumer[IO, Key, Value]
  type MyCommittableConsumerRecord  = CommittableConsumerRecord[IO, Key, Value]
  type PartitionStreams             = Stream[IO, MyCommittableConsumerRecord]

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

    readFrom(host, port, topicName, offset).take(3).compile.toList.map(_ => ExitCode.Success)
  }

  private def readFrom(host:      String,
                       port:      Int,
                       topicName: String,
                       offset:    Long): Stream[IO, Unit] = {
    val consumer: Stream[IO, MyKafkaConsumer] = consumerStream[IO].using(consumerSettings(host, port))

    val subscribeFn: MyKafkaConsumer => IO[Unit] = _.subscribeTo(topicName)


    val partitionStreamsFn: MyKafkaConsumer => Stream[IO, PartitionStreams] = { c =>
      c.partitionedStream
    }

    val printOut: MyCommittableConsumerRecord => IO[Unit] = { committable =>
      IO {
        println(s"committable = $committable")
      }
    }

    def setSeek(c: MyKafkaConsumer, topics: SortedSet[TopicPartition]): Stream[IO, MyKafkaConsumer] = {
      val ios: Set[IO[MyKafkaConsumer]] = topics.map { p =>
        println(s"setting offset")
        c.seek(p, offset).map(_ => c)
      }
      val seed: Stream[IO, MyKafkaConsumer] = Stream.empty.covary[IO]
      ios.foldLeft(seed) { case (a: Stream[IO, MyKafkaConsumer], c: IO[MyKafkaConsumer]) =>
        a ++ Stream.eval(c)
      }
    }

    def soughtAfter(c: MyKafkaConsumer): Stream[IO, MyKafkaConsumer] = {
       c.assignmentStream.flatMap { topics =>
        setSeek(c, topics)
      }
    }

    val s: Stream[IO, MyKafkaConsumer] = consumer.evalTap(subscribeFn).flatMap { c =>
      soughtAfter(c)
    }

    s.flatMap(partitionStreamsFn).flatMap{ cs =>
      cs.flatMap(c => Stream.eval(printOut(c)))
    }
  }

  /**
   * @tparam K KafkaConsumer
   * @tparam C CommittableConsumerRecord
   * @tparam P Output
   */
  def pipeline[K, C, P](s:                  Stream[IO, K],
                        subscribe:          K => IO[Unit],
                        forEachPartition:   Stream[IO, C] => Stream[IO, P],
                        partitionStreamsFn: K => Stream[IO, Stream[IO, C]]): Stream[IO, P] = {
    s.evalTap(subscribe).flatMap(partitionStreamsFn).flatMap(forEachPartition)
  }


  def forEachPartition[P, C](action: C => IO[P], s: Stream[IO, C]): Stream[IO, P] =
    s.flatMap { c => Stream.eval(action(c)) }
}
