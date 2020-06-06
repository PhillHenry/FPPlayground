package uk.co.odinconsultants.fp.cats.kleisli

import cats.Monad
import cats.data.{Kleisli, OptionT}

import scala.util.Try
import cats.implicits._

object MyKleisli {

  def stringToInt(x: String): Option[Int]     = Try { x.toInt }.toOption
  def divide10By(x: Int):     Option[Double]  = Try { 10d / x }.toOption

  def main(args: Array[String]): Unit = {
    val kStringToInt: Kleisli[Option, String, Int]    = Kleisli(stringToInt)
    val kDivision:    Kleisli[Option, Int, Double]    = Kleisli(divide10By)
    val combined:     Kleisli[Option, String, Double] = kStringToInt.andThen(kDivision)

    println(combined.run("5")) // Some(2.0)
  }

  /**
  Ben Spencer @dangerousben 14:16
I probably just don't know Kleisli well enough, but it feels like some stuff is easier with plain functions and some with kleislis
trying to implement something shaped like this, and it feels like it should be a lot easier than it's proving to be:
(A => Option[B], B => F[C], C => F[D]) => Kleisli[OptionT[F, *], A, D]
my inclination is to not bother applying Kleisli or OptionT until the end, but that means giving up on some useful facilities

Fabio Labella @SystemFw 14:21
follow your taste
going to the full kleisli first is probably helpful in deriving what the code should be, but it might not be the prettiest/more economical way

Fabio Labella @SystemFw 14:38
@dangerousben

one way
   */
  def foo[F[_]: Monad, A, B, C, D](
                                    a: A => Option[B],
                                    b: B => F[C],
                                    c: C => F[D]
                                  ): Kleisli[OptionT[F, *], A, D] = {

    def lift[X, Y] =
      Kleisli(_: X => F[Y]).mapK(OptionT.liftK)

    Kleisli { in: A =>
      OptionT(a(in).pure[F])
    } >>> lift(b) >>> lift(c)
  }

  def fooEquivalently[F[_]: Monad, A, B, C, D](
                                    a: A => Option[B],
                                    b: B => F[C],
                                    c: C => F[D]
                                  ): Kleisli[OptionT[F, *], A, D] = Kleisli { in =>
    OptionT
      .fromOption[F](a(in))
      .semiflatMap(b)
      .semiflatMap(c)
  }

}
