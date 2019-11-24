package uk.co.odinconsultants.fp.cats.fs2

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}

object SystemFWSharedStateMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val counter: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)

    def prog(c: Ref[IO, Int]): IO[Int] = for {
      _ <- c.update(_ + 1)
      v <- c.get
    } yield v

    def progUnsugared(c: Ref[IO, Int]): IO[Int] = {
      println("c.get = " + c.get)
      val updated: IO[Unit] = c.update(_ + 1)
      updated.flatMap { _ =>
        println("flatMap: c.get = ${c.get}, ")
        c.get
      }
    }

//    val main = counter.flatMap(c => prog(c))
    val main = counter.flatMap(c => progUnsugared(c))

    main.flatMap(x => IO { println(x) } ).map(_ => ExitCode.Success) // "1"
  }
}
