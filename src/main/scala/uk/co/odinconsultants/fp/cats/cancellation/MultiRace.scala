package uk.co.odinconsultants.fp.cats.cancellation

import cats.effect.concurrent.Deferred
import cats.effect.syntax.all._
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource}
import cats.implicits._

import scala.concurrent.duration._

/**
 * Aleksander Sumowski @aleksandersumowski 12:34
 * hi all, what would be a good way to extend IO.race to more then 2 tasks?
 *
 * Fabio Labella @SystemFw 12:48
 * do they all return the same thing?  things of the same type I mean
 *
 * Aleksander Sumowski @aleksandersumowski 12:48
 * yes
 */
object MultiRace extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    def multiRace[F[_]: Concurrent, A](fas: List[F[A]]): F[A] = {
      def spawn[B](fa: F[B]): Resource[F, Unit] =
        Resource.make(fa.start)(_.cancel).void

      def finish(fa: F[A], d: Deferred[F, Either[Throwable, A]]): F[Unit] =
        fa.attempt.flatMap(d.complete)

      Deferred[F, Either[Throwable, A]]
        .flatMap { result =>
          fas
            .traverse(fa => spawn(finish(fa, result)))
            .use(_ => result.get.rethrow)
        }
    }

    def program(i: Int) = {
      for {
        wait <- IO(scala.util.Random.nextInt(1000))
        _ <- IO(println(s"program $i waiting for $wait millis"))
        _ <- IO.sleep(wait.millis)
        _ <- IO(println(s"program $i finished"))
      } yield i
    }.guarantee(IO(println(s"program $i finalised")))

    def test = multiRace(List.range(0, 5).map(program))//.unsafeRunSync
    test.as(ExitCode.Success)
  }
}
