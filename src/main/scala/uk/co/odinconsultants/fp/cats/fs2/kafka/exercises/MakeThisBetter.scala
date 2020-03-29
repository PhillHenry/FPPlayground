package uk.co.odinconsultants.fp.cats.fs2.kafka.exercises

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, consumerStream}
import cats.implicits._
import scala.concurrent.duration._

object MakeThisBetter extends IOApp {

  val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")
      .withProperty("max.partition.fetch.bytes", "20485760")
      .withProperty("max.poll.records", "100000")

  def messageCount(topic: String): IO[Vector[Long]] = {
    consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      .chunks.evalMap(records =>
      for {
        ref <- Ref.of[IO, Long](0)
        count <- ref.modify(c => (c, c + records.size))
        _ <- IO(println(s"topic: $topic record size: ${records.size}"))
      } yield(count)
    )
      .interruptAfter(10.seconds)
      .compile
      .toVector
  }

  def process: IO[Unit] = {
    val topics = List (
      "tpch.customer", "tpch.lineitem", "tpch.nation", "tpch.orders", "tpch.part", "tpch.partsupp", "tpch.region", "tpch.supplier"
    )

    def counts = topics.parTraverse(messageCount).unsafeRunSync()

    val countSum = counts.map(c => c.sum)
    val msgCounts = topics.zip(countSum).sortBy(ct => ct._2)

    IO(println(s"message counts: $msgCounts"))
  }

  override def run(args: List[String]): IO[ExitCode] = {
    process.as(ExitCode.Success)
  }
}
