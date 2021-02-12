package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{ExitCode, IO, IOApp}

object GuaranteeMain extends IOApp {
  val aMessage = "Hello, world"

  val helloWorld: IO[String] = IO {
    val msg = "\noriginal message"
    println(msg)
    msg
  }

  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  val io       = helloWorld.guarantee(IO {
//    println(aMessage)
//    aMessage
//  }) // this prints out the message
  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  val printingIO: IO[Unit] = IO {
//    println(aMessage)
//    aMessage
//  }
//  val io2 = helloWorld.guarantee(printingIO) // this prints out the message too

  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  def printingIOWith(x: String): IO[Unit] = IO {
//    println(x)
//    x
//  }
//  val io3 = helloWorld.guarantee(printingIOWith(aMessage)) // this prints out the message too

  def printingIOWithT[T](x: => T): IO[T] = IO {
    println(s"IO passed '$x'")
    x
  }
  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  val io4 = helloWorld.guarantee(printingIOWithT("io4"))
  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  val io5 = helloWorld.guarantee(IO {
//    val msg = "io5"
//    println(s"IO printing '$msg'")
//    msg
//  })

  def printingIOWithT2[T](x: T): IO[T] = {
    println(s"Creating IO[$x]")
    IO {
      println(x)
      x
    }
  }
  /**
   * [ERROR] ... warning: discarded non-Unit value
   */
//  val io6 = helloWorld.guarantee(printingIOWithT2(aMessage))

  override def run(args: List[String]): IO[ExitCode] =
    (helloWorld /* *> io2 *> io3 *> io4 *> io5 *> io6 */).as(ExitCode.Success)
}
