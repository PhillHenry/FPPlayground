package uk.co.odinconsultants.fp.cats.validation

import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

trait ValidatedFixture {
  type MyDataType     = String
  type MyErrorType    = String
  type MyValidated[T] = Validated[MyErrorType, T]
  val invalid1Msg     = "invalid"
  val invalid2Msg     = "invalid2"
  val valid1          = Valid("valid1")
  val invalid1        = Invalid(invalid1Msg)
  val valid2          = Valid("valid2")
  val invalid2        = Invalid(invalid2Msg)
  val valid3          = Valid("valid3")
  val mixedList: NonEmptyList[Validated[MyErrorType, MyDataType]] = new NonEmptyList(valid1, List(invalid1, valid2, invalid2, valid3))
}
