package uk.co.odinconsultants.fp.cats.kleisli

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Sync

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

    def lift[X, Y]: (X => F[Y]) => Kleisli[OptionT[F, *], X, Y] =
      Kleisli(_: X => F[Y]).mapK(OptionT.liftK)

    Kleisli { in: A =>
      val value: F[Option[B]] = a(in).pure[F]
      OptionT(value)
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


  /*
Daniel Spiewak @djspiewak 04:50
thank you!
:-)
What is tagless with regards to cats-effect and why someone care about that?

"Tagless" is usually a synonym for "finally tagless", which is a somewhat loosely-defined way of organizing code around
polymorphic effects.

The idea is pretty simple: rather than writing def foo(str: String): IO[Unit], you write
def foo[F[_]](str: String): F[Unit]
or in practice, generally something like def foo[F[_]: Sync](str: String): F[Unit]
Now, Sync is less powerful than IO itself. If you see this signature, you know a lot more about the body of the function
than you would have had you just seen IO. Specifically, you know that, unless someone is doing very evil things, there is
no concurrency and no asynchrony.
There is suspension of side-effects though. So for example, foo may be a println function, like so:
def foo[F[_]: Sync](str: String): F[Unit] = Sync[F].delay(println(str))
Or it could be something else. We don't have a way of knowing. But we still learned some interesting things.
This is parametricity, and it helps quite a bit when you're trying to understand code by reading type signatures,
particularly in larger projects.
It's not the only thing we gain from polymorphism though.

Daniel Spiewak @djspiewak 04:56
Let's say that I'm writing the foo function, and I could have written it with IO, but instead I wrote it to be
polymorphic in F just to "do the right thing". Later on, I realize that all of the code around foo needs access to some
other service-layer part of the application. Say, the UserService. For example:

def bar[F[_]: Sync]: F[Unit] =
  getUserServiceSomehow.createUser("daniel").flatMap(u => foo("made the user!") >> getUserServiceSomehow.commitUser(u))

This isn't an unusual situation
So how do we define getUserServiceSomehow?
The easiest answer is using cats-mtl: we use Ask
def bar[F[_]: Sync](implicit users: Ask[F, UserService]): F[Unit] =
  users.ask.map(_.createUser("daniel")).flatMap(u => foo("made the user!") >> users.ask.map(_.commitUser(u)))
What's super-cool about this is we can compose this with other requirements as well, such as typed errors (with the
Raise and Handle typeclasses), state threading (with the Stateful class), and even other sorts of similar things.
For example, maybe I need UserService and ImageService:
def bar[F[_]: Sync](implicit users: Ask[F, UserService], images: Ask[F, ImageService]): F[Unit]
Lots of cool things here
First, you'll notice that we now know a lot about bar just by looking at the type. It performs side-effects, but only
synchronous ones (no concurrency!), but it also needs access to the UserService and the ImageService.
So our reasoning is really improved
Second, we haven't touched foo, the function we originally could have written hard-coded to IO, but didn't
This is the real beauty of polymorphism: flexibility.
See, IO can't possibly satisfy either Ask in the bar signature. In other words: F is not IO. It can't be. But if we had
hard-coded foo to IO, then we wouldn't be able to do any of this stuff that we decided to do long after we wrote foo.
Writing foo polymorphically, even though we didn't have to, gave us a ton of flexibility to refactor and evolve our code
down the line.

Daniel Spiewak @djspiewak 05:04
For the record, the way to satisfy these Ask instances is to use Kleisli. So, somewhere outside bar, as we're composing
this together, we probably have an instance of ImageService, so we can use Kleisli to pass it along and satisfy the type
constraints:

def baz[F[_]: Sync](implicit images: Ask[F, ImageService]): F[Unit] = {
  val userService: UserService = ???

  bar[Kleisli[F, UserService, *]].run(userService)
}

Note that the F here in baz is different than the one that's in bar. The one in baz has the capability of capturing
synchronous side-effects, and it has the ability to get an ImageService, but it doesn't have the ability to get a
UserService. (we know that by looking at baz's type!) However, bar needs that UserService, so we hand it one by
instantiating its F to be Kleisli and then running that Kleisli using the userService we were able to construct.

This is the power of polymorphism. "Tagless" is one technique that is often used to organize your code around
polymorphic effects.
Cats Effect interrelates with this in important ways because Cats Effect defines what it means to "have the capability
of capturing side-effects", as well as what it means to "have the capability of running things concurrently". These are
very important capabilities to define, and you can't really write a real application in a polymorphic style without them.
   */
  trait UserService

  trait ImageService

  class Ask[F[_], T]

  def bar[F[_]: Sync](implicit users: Ask[F, UserService], images: Ask[F, ImageService]): F[Unit] = ???
  def barNonAsk[F[_]: Sync](implicit users: UserService, images: ImageService): F[Unit] = ???

  def baz[F[_]: Sync](implicit images: Ask[F, ImageService]): F[Unit] = {
    val userService: UserService = ???

    implicit val askUser:   Ask[F, UserService]   = ???
    implicit val askImage:  Ask[F, ImageService]  = ???

    implicit val userServiceImplicit: UserService   = ???
    implicit val imageService:        ImageService  = ???

//    bar[Kleisli[F, UserService, *]].run(userService)
    val x: Kleisli[F, UserService, Unit] = barNonAsk[Kleisli[F, UserService, *]] //.run(userService)

    val result: F[Unit] = x.run(userService)

    ???
  }

}
