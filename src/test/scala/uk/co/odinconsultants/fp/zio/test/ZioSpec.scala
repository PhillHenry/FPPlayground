package uk.co.odinconsultants.fp.zio.test

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{Assertion, DefaultRunnableSpec, TestResult, ZSpec, assert, suite, testM}
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.TestAspect._

object ZioSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = suite("Me playing around with ZIO test")(
    testM("Problem I faced that is documented at http://javaagile.blogspot.com/2015/03/interesting-syntax-but-does-it-makes.html"){
      val result:   Option[String]            = None
      val asserted: Option[TestResult]        = result.map(x => assert(x)(equalTo("this is naive as it assumes happy path")))
      val myZio:    zio.IO[Unit, TestResult]  = ZIO.fromOption(asserted)
      myZio
    } @@ ignore // ignore because this test is deliberately imperfect
      ,
    testM("1=1 obviously passes") {
      val shouldSatisfy:  Assertion[Int] => TestResult  = assert(1)
      val assertion:      Assertion[Any]                = equalTo(1)
      ZIO(shouldSatisfy(assertion))
    }
      ,
    testM("Problem I faced that is documented at http://javaagile.blogspot.com/2015/03/interesting-syntax-but-does-it-makes.html"){
      val result:   Either[Throwable, String]       = Left(new Exception("The unhappy path"))
      val asserted: Either[Throwable, TestResult]   = result.map(x => assert(x)(equalTo("this is naive as it assumes happy path")))
      val myZio:    ZIO[Any, Throwable, TestResult] = ZIO.fromEither(asserted)
      myZio
    } @@ ignore // ignore because this test is deliberately imperfect
  )
}
