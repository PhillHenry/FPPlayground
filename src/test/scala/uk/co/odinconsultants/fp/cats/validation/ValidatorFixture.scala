package uk.co.odinconsultants.fp.cats.validation

import cats.implicits._

trait ValidatorFixture extends ValidationFixture {

  val o1 = Option(first)
  val o2 = Option(second)
  val o3 = Option(third)
  val validatesOption = new Validator(o1, o2, o3)

  type MyEither = Either[Exception, String]
  val r1: MyEither = Right(first)
  val r2: MyEither = Right(second)
  val r3: MyEither = Right(third)
  val validatesEither = new Validator(r1, r2, r3)

}
