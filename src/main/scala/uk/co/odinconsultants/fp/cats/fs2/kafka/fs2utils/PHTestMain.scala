package uk.co.odinconsultants.fp.cats.fs2.kafka.fs2utils

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

import scala.concurrent.Await
import scala.concurrent.duration._

object PHTestMain extends IOApp {

  case class MyDatum(id: Int, value: String)

  val selector: MyDatum => Int = _.id

  def datumFor(i: Int): MyDatum = MyDatum(i, i.toString)
  def streamFor(n: Int): Stream[IO, MyDatum] = Stream.eval(IO {
    println(s"n = $n")
    datumFor(n)
  }).repeatN(n)

  val s = (2 to 10).foldLeft(streamFor(1)) { case (acc, i) =>
    acc.interleave(
      streamFor(i)
    )
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val ioList: IO[List[MyDatum]] = s.take(55).compile.toList
    val xs    = ioList.unsafeToFuture()
    val data  = Await.result(xs, 1 second)
    println(s"data:\n${data.mkString("\n")}")
    ioList.map(_ => ExitCode.Success)
  }
}
