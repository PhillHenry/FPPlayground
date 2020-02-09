package uk.co.odinconsultants.fp.cats.effects.cancellation
import cats.Monad
import cats.effect.{ Resource, IO, ContextShift, Timer }
import cats.implicits._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
Fabio Labella @SystemFw Feb 06 21:05
@kory33 check the entirety of the doc :)

If the outer F fails or is interrupted, allocated guarante that the finalizers will be called.
        However, if the outer F succeeds, it's up to the user to ensure the returned F[Unit] is called once A needs to be released.
        If the returned F[Unit] is not called, the finalizers will not be run. <--- THIS
        For this reason, this is an advanced and potentially unsafe api which can cause a resource leak if not used correctly,
        please prefer [[use]] as the standard way of running a Resource program.

in your case allocated succeeds, and after the flatMap is up to you to guarantee that the returned F[Unit] is called, and you are discarding it in case (id, _).
        I tried to make the scaladoc as scary as possible: allocated is fine if you're using it in a test in conjunction with unsafeRunSync ,
        for example to expose a Resourceto before and after in test frameworks, but if you are using it in concurrent
        code you need to understand the cats-effect model really well to make things work correctly.

As a rule of thumb, you basically have to do allocated.continual, and even then there is often a better way
        (for example in the implementation of parZip I've worked on the AST directly).
        This is due to the fact that Resource fundamentally keeps track of the F[Unit] finaliser for you,
        and when you call allocated you are saying "give it back to me because I need to do my own non-standard handling,
        I promise I'll take care of calling it when I need to"

 */
object WhenIsItCancelled extends App  {

  implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def printForever(id: Int): IO[Nothing] = Monad[IO].foreverM(IO { println(id) } *> IO.sleep(1.second))
  val resource: Resource[IO, Int] = Resource.make(IO.pure(1))(x => IO { println(s"releasing $x") })

  (for {
    f <- resource.allocated.flatMap { case (id, io) =>
//      io.flatMap(_ => printForever(id) ) // this fixes the lack of "releasing 1" message per Fabio's diagnoses. But note the output is still not the same as below
      printForever(id)
    }.start
    _ <- IO { println("allocated") }
    _ <- IO.sleep(3.second) *> f.cancel
    _ <- IO { println("cancelled") }
  } yield ()).unsafeRunSync()

  println("finish\n\n")

  (for {
    f <- resource.use { id => printForever(id) }.start
    _ <- IO { println("allocated") }
    _ <- IO.sleep(3.second) *> f.cancel
    _ <- IO { println("cancelled") }
  } yield ()).unsafeRunSync()

  println("finish")
}
