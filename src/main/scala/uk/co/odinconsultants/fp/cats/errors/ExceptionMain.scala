package uk.co.odinconsultants.fp.cats.errors

import cats.{Applicative, Defer}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

/**
Daniel Spiewak @djspiewak Oct 12 16:06
so a concern about something like the above is Defer only gives you laziness
but it doesn't really talk about how it is lazy
for example, imagine that we had done this:
def foo[F[_]: Applicative: Defer](): F[Unit] = {
  val eff = Defer[F].defer(Applicative[F].pure(println("side effect")))
  eff *> eff
}
nothing weird about that, and it'll work just fine if you call foo[IO]
however, it might not work fine if you call foo with Eval!
(and Eval is a valid member here)
Eval is free to memoize things, and in fact in some constructors, it does exactly that
memoizing would mean that we don't re-run the effect, which is actually pretty important
Another problem shows up if you have errors
def foo[F[_]: Applicative: Defer](): F[Unit] =
  Defer[F].defer(Applicative[F].pure(throw Exception("boom")))
IO will catch that, Eval won't, and both are valid instantiations for F
so Sync is definitely what you want here :-)
basically, there are a lot of subtle cases surrounding effect capture, and Sync/Async are properly constrained such
that implementations are not allowed to behave poorly in those cases
Defer is just… stack safety
Fabio Labella @SystemFw Oct 12 16:21
much better answer :)
 */
object ExceptionMain extends IOApp {

  val printFinished: IO[Unit] = IO {
    println("Finished!")
  }

  override def run(args: List[String]): IO[ExitCode] = {
    (foo[IO] *> printFinished).map(_ => ExitCode.Success) // throws exception and does not print line
//    foo[Defer] .map(_ => ExitCode.Success)
  }

  def foo[F[_]: Applicative: Defer](): F[Unit] =
    Defer[F].defer(Applicative[F].pure(throw new Exception("boom")))

  def bar(): IO[Unit] = IO { throw new Exception("boom") }
}
