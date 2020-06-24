package uk.co.odinconsultants.fp.zio.test

import zio.test._
import zio.test.Assertion._
import zio.{Ref, ZIO}
import zio.test.environment.TestEnvironment
import zio.test.environment.TestSystem.Test
import zio._
import zio.test.TestAspect._


/**
 * @see https://stackoverflow.com/questions/58662285/how-to-test-an-exception-case-with-zio-test
 */
object UnhappyPathSpec
  extends DefaultRunnableSpec {

  type Results = Map[String, Either[Exception, String]]

  val exceptionA    = new Exception("A")
  val exceptionB    = new Exception("B")
  val badA          = Left(exceptionA)
  val badB          = Left(exceptionB)
  val stringC       = "Good C"
  val stringD       = "Good D"
  val goodC         = Right(stringC)
  val goodD         = Right(stringD)
  val kvs: Results  = Map("A" -> badA, "B" -> badB, "C" -> goodC, "D" -> goodD)

  override def spec: ZSpec[TestEnvironment, Any] = suite("ExampleSpec")(
    testM("Example of testing for expected failure") {
      val io: IO[String, Nothing] = ZIO.fail("fail")
      for {
        result <- io.run
      } yield assert(result)(fails(equalTo("fail")))
    }
    ,
    testM("aggregating exceptions after removing Rights") {
      for {
        result <- ZIO.collectAll(kvs.filter{ case (_, v) => v.isLeft }.map { case (_, v) => ZIO.fromEither(v).flip }.toList)
      } yield assert(result)(equalTo(List(exceptionA, exceptionB)))
    }
    ,
    testM("aggregating exceptions (io.run)") {
      val io: IO[String, List[Exception]] = ZIO.collectAll(kvs.map { case (k, v) => ZIO.fromEither(v).flip }.toList)
      for {
        result <- io.run
      } yield assert(result)(fails(equalTo(stringC)))
    }
    ,
    testM("aggregating exceptions") {
      val io: IO[String, List[Exception]] = ZIO.collectAll(kvs.map { case (k, v) => ZIO.fromEither(v).flip }.toList)
      for {
        result <- io
      } yield assert(result)(equalTo(List(new Exception(stringC))))
    } @@ ignore
    ,
    testM("Example of orDie") {
      for {
        result <- ZIO(???).orDie
      } yield assert(result)(equalTo("hello"))
    } @@ ignore
  )
}

