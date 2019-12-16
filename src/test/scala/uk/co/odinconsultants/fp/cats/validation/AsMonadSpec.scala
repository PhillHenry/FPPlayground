package uk.co.odinconsultants.fp.cats.validation

import cats.data.NonEmptyList
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
@RunWith(classOf[JUnitRunner])
class AsMonadSpec extends WordSpec with Matchers {

  "Either" should {
    "be applicable" in new EitherFixture {
      import cats.implicits._
      val x = new AsMonad[MyValidated, Throwable]
      x.allOrNothing(mixedList) shouldBe invalid1
    }
    "be flatmappable" in new EitherFixture {
      import cats.implicits._
      val x = new AsMonad[MyValidated, Throwable]
      val allGood = new NonEmptyList(valid1, List(valid2))
      val result = for {
        y <- x.allOrNothing(allGood)
        z <- x.allOrNothing(allGood)
      } yield z
      println(result)
      result shouldBe valid2
    }
  }
  "EitherNel" should {
    import cats.data.EitherNel
    type MyErrorType    = Throwable
    type MyEitherNel[T] = EitherNel[MyErrorType, T]
    type MyDataType     = MyEitherNel[String]

    val good: MyDataType = Right("success!")
    val bad:  MyDataType = Left(NonEmptyList(new Throwable("failure"), List()))
    val mixedList: NonEmptyList[MyDataType] = NonEmptyList(good, List(good, bad))

    "be applicable" in {
      import cats.implicits._
//      val x = new AsMonad[MyEitherNel, MyErrorType]
//      x.allOrNothing(mixedList) shouldBe bad
    }
    "be flatmappable" in {
      import cats.implicits._
//      val x = new AsMonad[MyEitherNel, MyErrorType]
//      val allGood = new NonEmptyList(good, List(good))
//      val result = for {
//        y <- x.allOrNothing(allGood)
//        z <- x.allOrNothing(allGood)
//      } yield z
//      println(result)
//      result shouldBe good
    }
  }

}
