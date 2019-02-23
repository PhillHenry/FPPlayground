package uk.co.odinconsultants.fp.cats.effects

import cats.effect.{IO, Resource}
import cats.implicits._

/**
  * @see https://typelevel.org/cats-effect/datatypes/resource.html
  */
object MyResource extends App {

  val acquire: IO[String] = IO(println("Acquire cats...")) *> IO("cats")

  val greet: String => IO[Unit] = x => IO(println("Hello " ++ x))

  val effect = Resource.liftF(IO.pure("World")).use(greet)
  effect.unsafeRunSync

}
