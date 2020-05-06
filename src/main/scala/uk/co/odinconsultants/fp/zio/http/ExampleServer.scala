package uk.co.odinconsultants.fp.zio.http

import java.net.InetSocketAddress

import uzhttp.Response
import uzhttp.server.Server
import zio.ZIO
import zio._

object ExampleServer extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Server
      .builder(
        address = new InetSocketAddress("127.0.0.1", 8000)
      )
      .handleAll { _ => ZIO.succeed(Response.plain("Hello World\n")) }
      .serve
      .useForever
      .orDie
}
