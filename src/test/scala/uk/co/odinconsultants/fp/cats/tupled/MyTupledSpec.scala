package uk.co.odinconsultants.fp.cats.tupled

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
@RunWith(classOf[JUnitRunner])
class MyTupledSpec extends WordSpec with Matchers {

  "Left" should {
    "short circuit" in {
      import cats.implicits._
      type MyEither = Either[String, String]
      val e1: MyEither = Left("e1")
      val e2: MyEither = Left("e2")
      (e1, e2).tupled shouldBe e1
    }
  }

}
