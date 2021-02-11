package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{ExitCode, IO, IOApp}

object GuaranteeMain extends IOApp {
  val aMessage = "Hello, world"

  val helloWorld: IO[String] = IO {
    val msg = "\noriginal message"
    println(msg)
    msg
  }

  val io       = helloWorld.guarantee(IO {
    println(aMessage)
    aMessage
  }) // this prints out the message

  val printingIO: IO[Unit] = IO {
    println(aMessage)
    aMessage
  }
  val io2 = helloWorld.guarantee(printingIO) // this prints out the message too

  def printingIOWith(x: String): IO[Unit] = IO {
    println(x)
    x
  }
  val io3 = helloWorld.guarantee(printingIOWith(aMessage)) // this prints out the message too

  def printingIOWithT[T](x: T): IO[T] = IO {
    println(x)
    x
  }
  val io4 = helloWorld.guarantee(printingIOWithT(aMessage))

  val io5 = helloWorld.guarantee(IO {
    println(aMessage)
    aMessage
  })

  def printingIOWithT2[T](x: T): IO[T] = {
    println(s"Creating IO[$x]")
    IO {
      println(x)
      x
    }
  }
  val io6 = helloWorld.guarantee(printingIOWithT2(aMessage))

  override def run(args: List[String]): IO[ExitCode] =
    (io *> io2 *> io3 *> io4 *> io5 *> io6).as(ExitCode.Success)
}
