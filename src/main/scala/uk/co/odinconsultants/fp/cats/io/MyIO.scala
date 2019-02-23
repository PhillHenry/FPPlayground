package uk.co.odinconsultants.fp.cats.io

import cats.effect.IO
import uk.co.odinconsultants.fp.cats.io.MyIO.putStrlLn

/**
  * @see https://typelevel.org/cats-effect/datatypes/io.html
  */
object MyIO extends App {

  def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
    IO(a + b).flatMap { b2 =>
      if (n > 0)
        fib(n - 1, b, b2)
      else
        IO.pure(b2)
    }

  def putStrlLn(value: String) = IO(println(value))
  val readLn = IO(scala.io.StdIn.readLine)

  val cliIO = for {
    _ <- putStrlLn("What's your name?")
    n <- readLn
    _ <- putStrlLn(s"Hello, $n!")
  } yield ()
//  cliIO.unsafeRunSync() // does what you expect - runs the program


  val fibIO = for {
    _ <- putStrlLn("Starting fibonacci ")
    x <- fib(10)
    _ <- putStrlLn(x.toString) // x is a Long
  } yield ()
  fibIO.unsafeRunSync()


  val ioa = IO { println("hey!") }

  val program: IO[Unit] =
    for {
      _ <- ioa
      _ <- ioa
    } yield ()

//  program.unsafeRunSync() // prints "hey!" twice
}
