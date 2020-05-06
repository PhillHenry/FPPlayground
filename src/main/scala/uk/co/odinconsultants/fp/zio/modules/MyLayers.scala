package uk.co.odinconsultants.fp.zio.modules

import zio._

import scala.util.control.NoStackTrace
import zio.{App, ZIO}
import zio.console.Console

case class UserId(x: Int)

case class User(id: UserId, name: String)

class DBError extends NoStackTrace


/**
 * @see https://zio.dev/docs/howto/howto_use_layers
 */
object MyLayers extends App {

  type UserRepo = Has[UserRepo.Service]

  object UserRepo {
    trait Service {
      def getUser(userId: UserId): IO[DBError, Option[User]]
      def createUser(user: User): IO[DBError, Unit]
    }


    // This simple live version depends only on a DB Connection
    val inMemory: Layer[Nothing, UserRepo] = ZLayer.succeed(
      new Service {
        def getUser(userId: UserId): IO[DBError, Option[User]] = UIO(None)
        def createUser(user: User): IO[DBError, Unit] = UIO(println(s"Pretending to create $user"))
      }
    )

    //accessor methods
    def getUser(userId: UserId): ZIO[UserRepo, DBError, Option[User]] =
      ZIO.accessM(_.get.getUser(userId))

    def createUser(user: User): ZIO[UserRepo, DBError, Unit] =
      ZIO.accessM(_.get.createUser(user))
  }

  type Logging = Has[Logging.Service]

  object Logging {
    trait Service {
      def info(s: String): UIO[Unit]
      def error(s: String): UIO[Unit]
    }


    import zio.console.Console
    val consoleLogger: ZLayer[Console, Nothing, Logging] = ZLayer.fromFunction( console =>
      new Service {
        def info(s: String): UIO[Unit]  = console.get.putStrLn(s"info - $s")
        def error(s: String): UIO[Unit] = console.get.putStrLn(s"error - $s")
      }
    )

    //accessor methods
    def info(s: String): ZIO[Logging, Nothing, Unit] =
      ZIO.accessM(_.get.info(s))

    def error(s: String): ZIO[Logging, Nothing, Unit] =
      ZIO.accessM(_.get.error(s))
  }

//  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val user2: User = User(UserId(123), "Tommy")
    val makeUser: ZIO[Logging with UserRepo, DBError, Unit] = for {
      _ <- Logging.info(s"inserting user")  // ZIO[Logging, Nothing, Unit]
      _ <- UserRepo.createUser(user2)       // ZIO[UserRepo, DBError, Unit]
      _ <- Logging.info(s"user inserted")   // ZIO[Logging, Nothing, Unit]
    } yield ()

    val logger: ZLayer[Console, Nothing, Logging] = Logging.consoleLogger
    val memory: Layer[Nothing, UserRepo] = UserRepo.inMemory
    // compose horizontally
    val horizontal: ZLayer[Console, Nothing, Logging with UserRepo] = logger ++ memory

    // fulfill missing deps, composing vertically
    val fullLayer: Layer[Nothing, Logging with UserRepo] = Console.live >>> horizontal

    // provide the layer to the program
    val result:   ZIO[Any, DBError, Unit] = makeUser.provideLayer(fullLayer)
    val exit:     UIO[Int]                = UIO(1)    // UIO[+A]      = ZIO[Any, Nothing, A]

    val exitResult: ZIO[Any, DBError, Unit] = exit *> result

    val resultExit: ZIO[Any, DBError, Int] = result *> exit

    resultExit.catchAll(_ => exit)
  }
}
