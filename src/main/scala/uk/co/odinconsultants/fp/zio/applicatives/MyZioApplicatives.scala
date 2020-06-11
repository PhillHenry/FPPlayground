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
    val fail = ZIO.fail(new Exception("yoiks!"))

    val applicatives: ZIO[Any, Throwable, Nothing] = ok1 *> ok2 *> fail

    applicatives.fold(_ => 1, _ => 0)
  }
}
