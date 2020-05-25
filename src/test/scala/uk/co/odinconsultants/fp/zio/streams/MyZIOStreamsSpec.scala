package uk.co.odinconsultants.fp.zio.streams

import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion._
import zio.test.TestAspect._

object MyZIOStreamsSpec extends DefaultRunnableSpec {

  import MyZIOStreams._

  val first5Fibs = List(0, 1, 1, 2, 3)

  override def spec: ZSpec[TestEnvironment, Any] = suite("Streams")(
    testM("interleaved should be a mix of Unit and pure values") {
      assertM(interleaved.take(5).runCollect)(not(equalTo(first5Fibs))) // because it has some Units in there
    }
      ,
    testM("forked should only collect pure") {
      assertM(fibForkPrint.take(5).runCollect)(equalTo(first5Fibs))
    }
      ,
    testM("should evaluate all but only collect pure") {
      assertM(concatenated.take(5).runCollect)(equalTo(first5Fibs))
    } @@ ignore
  )

}
