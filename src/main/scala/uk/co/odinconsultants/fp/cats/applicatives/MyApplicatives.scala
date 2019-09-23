package uk.co.odinconsultants.fp.cats.applicatives

import cats.{Applicative, Monad}
import cats.implicits._
import cats.data._

class MyApplicatives[T[_]: Applicative](T1: T[String], T2: T[String], T3: T[String]) {

  case class MyStruct(x: String, y: String, z: String)

  def process: T[MyStruct] = (T1, T2, T3).mapN(MyStruct)

}