package uk.co.odinconsultants.fp.monads

/**
  * @see https://scalaz.github.io/scalaz-zio/overview/
  */
object ProgramsAsValues {

  sealed trait Console[+A] { self =>
    def map[B](f: A => B): Console[B] =
      flatMap(a => success(f(a)))

    def flatMap[B](f: A => Console[B]): Console[B] =
      self match {
        case Return(value) => f(value())
        case PrintLine(line, rest) =>
          PrintLine(line, rest.flatMap(f))
        case ReadLine(rest) =>
          ReadLine(line => rest(line).flatMap(f))
      }
  }

  case class Return[A](value: () => A) extends Console[A]
  case class PrintLine[A](line: String, rest: Console[A]) extends Console[A]
  case class ReadLine[A](rest: String => Console[A]) extends Console[A]

  def interpret[A](program: Console[A]): A = program match {
    case Return(value) => value()
    case PrintLine(line, rest) => println(line); interpret(rest)
    case ReadLine(rest) => interpret(rest(scala.io.StdIn.readLine()))
  }

  def printLine(line: String): Console[Unit] =
    PrintLine(line, Return(() => line))

  val readLine: Console[String] =
    ReadLine(line => Return(() => line))

  def success[A](a: A): Console[A] = Return(() => a)

  val program: Console[String] =
    for {
      _    <- printLine("What's your name?")
      name <- readLine
      _    <- printLine(s"Hello, ${name}, good to meet you!")
    } yield name

  def main(args: Array[String]): Unit = {
    interpret(program)
  }
}
