package uk.co.odinconsultants.fp.cats.monads

import cats.Monad
import cats.effect.IO

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * https://stackoverflow.com/questions/33149471/why-do-monads-not-compose-in-scala
  */
object Composition extends App {

  import scala.concurrent.ExecutionContext.Implicits._
  import cats.data.OptionT
  import cats.implicits._

  // we also need monadic transformer OptionT which will work only for Option (precisely F[Option[T]])
  def monadTransformers: OptionT[Future, Int] = {
    val fa = OptionT[Future, Int](Future(Some(1)))
    val fb = OptionT[Future, Int](Future(Some(2)))
    val x: OptionT[Future, Int] = fa.flatMap(a => fb.map(b => a + b)) //note that a and b are already Int's not Future's

    val equivalently: OptionT[Future, Int] = for {
      a <- fa
      b <- fb
    } yield a + b

    println(Await.result(equivalently.value, 1.seconds))
    println(Await.result(transformAndAdd(fa, fb).value, 1.seconds))

    val faIO = OptionT[IO, Int](IO(Some(1)))
    val fbIO = OptionT[IO, Int](IO(Some(2)))
    println("faIO, fbIO: " + transformAndAdd(faIO, fbIO).value.unsafeRunSync())

    x
  }

  def transformAndAdd[F[_]: Monad](fa: OptionT[F, Int], fb: OptionT[F, Int]): OptionT[F, Int] = for {
    a <- fa
    b <- fb
  } yield a + b

  // Important thing here is that there is no FutureT defined in cats, so you can compose Future[Option[T]], but can't do that with Option[Future[T]] (later I'll show that this problem is even more generic).
  // On the other hand, if you choose composition using Applicative, you'll have to meet only one requirement: both Future and Option should have Applicative instances defined over them


  def applicative = {
//    import cats._
    import cats.data.Nested
    import cats.implicits._
//    import cats.instances.all._
//    val fa = Nested[Future, Option, Int](Future(Some(1)))
//    val fb = Nested[Future, Option, Int](Future(Some(1)))
//    fa.map(x => (y: Int) => y + x).ap(fb) // Argh! says map not a member of Nested
    import cats.data.Validated
    import cats.data.Validated.Valid
//    val nested: Nested[Option, Validated[String, ?], Int] = Nested(Some(Valid(123)))
//    nested.map(_.toString).value
  }


  println(Await.result(monadTransformers.value, 1.seconds))

}
