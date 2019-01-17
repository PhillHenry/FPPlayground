package uk.co.odinconsultants.fp.cats.monads

import scala.concurrent.Future

/**
  * https://stackoverflow.com/questions/33149471/why-do-monads-not-compose-in-scala
  */
object Composition extends App {

  import scala.concurrent.ExecutionContext.Implicits._

  // we also need monadic transformer OptionT which will work only for Option (precisely F[Option[T]])
  def monadTransformers = {
    import cats.data.OptionT
    import cats.implicits._
    val fa = OptionT[Future, Int](Future(Some(1)))
    val fb = OptionT[Future, Int](Future(Some(2)))
    fa.flatMap(a => fb.map(b => a + b)) //note that a and b are already Int's not Future's
  }

  // Important thing here is that there is no FutureT defined in cats, so you can compose Future[Option[T]], but can't do that with Option[Future[T]] (later I'll show that this problem is even more generic).
  // On the other hand, if you choose composition using Applicative, you'll have to meet only one requirement: both Future and Option should have Applicative instances defined over them


  def applicative = {
    import cats._
    import cats.data._
    import cats.implicits._
    val fa = Nested[Future, Option, Int](Future(Some(1)))
    val fb = Nested[Future, Option, Int](Future(Some(1)))
//    fa.map(x => (y: Int) => y + x).ap(fb) // Argh! says map not a member of Nested
  }

  println(applicative)

}
