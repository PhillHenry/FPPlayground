package uk.co.odinconsultants.fp.cats.validation

import cats.data.NonEmptyList

trait EitherFixture {
  type MyDataType                                       = String
  type MyErrorType                                      = Throwable
  type MyValidated[T]                                   = Either[MyErrorType, T]
  val invalid1Msg                                       = "invalid"
  val invalid2Msg                                       = "invalid2"
  val valid1Msg                                         = "valid1"
  val valid1:   Either[MyErrorType, MyDataType]         = Right(valid1Msg)
  val throwable1                                        = new Throwable(invalid1Msg)
  val invalid1: Either[MyErrorType, MyDataType]         = Left(throwable1)
  val valid2:   Either[MyErrorType, MyDataType]         = Right("valid2")
  val throwable2                                        = new Throwable(invalid2Msg)
  val invalid2: Either[MyErrorType, MyDataType]         = Left(throwable2)
  val valid3:   Either[MyErrorType, MyDataType]         = Right("valid3")

  val mixedList: NonEmptyList[Either[MyErrorType, MyDataType]]
    = new NonEmptyList(valid1, List(invalid1, valid2, invalid2, valid3))
}
