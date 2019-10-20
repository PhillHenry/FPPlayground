package uk.co.odinconsultants.fp.cats.validation

import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

trait ValidatedFixture {
  type MyDataType     = String
  type MyErrorType    = String
  type MyValidated[T] = Validated[MyErrorType, T]
  val invalid1Msg     = "invalid"
  val invalid2Msg     = "invalid2"
  val valid1Msg       = "valid1"
  val valid1:   Validated[String, String]          = Valid(valid1Msg)
  val invalid1: Validated[String, String]          = Invalid(invalid1Msg)
  val valid2:   Validated[String, String]          = Valid("valid2")
  val invalid2: Validated[String, String]          = Invalid(invalid2Msg)
  val valid3:   Validated[String, String]          = Valid("valid3")
  val mixedList: NonEmptyList[Validated[MyErrorType, MyDataType]] = new NonEmptyList(valid1, List(invalid1, valid2, invalid2, valid3))

  val invalidThrowable1:  Validated[Throwable, String] = Invalid(new Throwable(invalid1Msg))
  val validThrowable1:    Validated[Throwable, String] = Valid(valid1Msg)
}
