package uk.co.odinconsultants.fp.cats.fs2.socket

import java.net.InetSocketAddress

import cats.effect.{Blocker, Concurrent, ContextShift, ExitCode, IO, IOApp, Timer}
import fs2.{Stream, _}
import fs2.io.tcp.SocketGroup

import scala.concurrent.duration._
import cats.implicits._

object ServerSocketMain extends IOApp {

  val address = new InetSocketAddress("127.0.0.1", 0)

  override def run(args: List[String]): IO[ExitCode] = {
    client[IO].as(ExitCode.Success)
  }

  def client[F[_]: Concurrent: ContextShift: Timer]: F[Unit] =
    Blocker[F].use { blocker =>
      SocketGroup[F](blocker).use { socketGroup =>
        socketGroup.server(address).map { connection =>
          Stream.resource(connection).flatMap { socket =>
            Stream
              .range(0, 10)
              .map(i => s"Ping no $i \n")
              .covary[F]
              .metered(1.second)
              .through(text.utf8Encode)
              .through(socket.writes())
              .onFinalize(socket.endOfOutput)
          }
        }.parJoin(50)
          .interruptAfter(10.minutes).compile.drain
      }
    }
}
