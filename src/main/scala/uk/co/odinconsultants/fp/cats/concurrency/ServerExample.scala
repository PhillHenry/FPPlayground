package uk.co.odinconsultants.fp.cats.concurrency

object ServerExample {
  import cats.effect._
  import cats.effect.syntax.all._
  import cats.effect.ExitCase._
  import cats.implicits._
  import java.net.{ServerSocket, Socket}

  // echoProtocol as defined before
  def echoProtocol[F[_]: Sync](clientSocket: Socket): F[Unit] = ???

  def serve[F[_]: Concurrent](serverSocket: ServerSocket): F[Unit] = {
    def close(socket: Socket): F[Unit] =
      Sync[F].delay(socket.close()).handleErrorWith(_ => Sync[F].unit)

    for {
      _ <- Sync[F]
        .delay(serverSocket.accept())
        .bracketCase { socket =>
          echoProtocol(socket)
            .guarantee(close(socket))                 // Ensuring socket is closed
            .start                                    // Will run in its own Fiber!
        }{ (socket, exit) => exit match {
          case Completed => Sync[F].unit
          case Error(_) | Canceled => close(socket)
        }}
      _ <- serve(serverSocket)                  // Looping back to the beginning
    } yield ()
  }
}
