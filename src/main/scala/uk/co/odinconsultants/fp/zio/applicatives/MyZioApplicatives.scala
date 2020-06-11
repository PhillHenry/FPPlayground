package uk.co.odinconsultants.fp.zio.applicatives

import zio._

object MyZioApplicatives extends App {

  def zio(x: String) = ZIO {
    println(s"ZIO: $x")
    x
  }

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val ok1 = zio("first")
    val ok2 = zio("second")
    val fail = ZIO.fail {
      Thread.sleep(1000)
      new Exception("yoiks!")
    }

    val applicatives: ZIO[Any, Throwable, String] = ok1 *> fail *> ok2

    applicatives.fold(_ => 1, _ => 0)
  }
}
