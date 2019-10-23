package uk.co.odinconsultants.fp.cats.applicatives

import cats.data.NonEmptyList

trait EitherStringFixture {

  type MyEither[T] = Either[String, T]

  val success1: MyEither[String] = Right("success1")
  val success2: MyEither[String] = Right("success2")
  val failureMsg1 = "failure1"
  val failureMsg2 = "failure2"
  val failure1: MyEither[String] = Left(failureMsg1)
  val failure2: MyEither[String] = Left(failureMsg2)

  val mixed = NonEmptyList.of(success1, failure1, success2, failure2)

}
