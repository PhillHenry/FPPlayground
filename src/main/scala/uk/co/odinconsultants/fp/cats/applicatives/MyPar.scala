package uk.co.odinconsultants.fp.cats.applicatives

import cats.{Applicative, Parallel}
import cats.data.{EitherNec, Validated}

object MyPar {

  def concatStrings(x: String, y: String, z: String): String = {
    println(s"${Thread.currentThread().getName}: concatStrings: x= $x, y = $y, z = $z")
    s"x = $x, y = $y, z = $z"
  }

  def parToAccumulateErrors[T[String]: Applicative](t1: T[String], t2: T[String], t3: T[String]) = {
    import cats.implicits._
//    (t1, t2, t3).parMapN(concatStrings)
  }

  type ValidatedAcc = Validated[String, String]
  def parToAccumulateErrorsInValidated(t1: ValidatedAcc, t2: ValidatedAcc, t3: ValidatedAcc) = {
    import cats.implicits._
//    implicit val P = Parallel[EitherNec[String, *]]
//    implicit val Q = Parallel[Validated[String, *]]
//    (t1, t2, t3).parMapN(concatStrings)
  }
  type EitherNecAcc = EitherNec[String, String]
  def parToAccumulateErrorsInEitherNec(t1: EitherNecAcc, t2: EitherNecAcc, t3: EitherNecAcc): EitherNecAcc = {
    import cats.implicits._
    val tupled = (t1, t2, t3)
    val x: EitherNec[String, (String, String, String)] = tupled.parTupled //.parMapN(concatStrings)
    tupled.parMapN(concatStrings)
  }

  case class Make(name: String)
  case class Model(name: String)
  case class Description(text: String)
  case class MakeModel(make: Make, model: Model)
  case class Listing(makeModel: MakeModel, description: Description)

  def validateMake(name: String): EitherNec[String, Make] = ???
  def validateModel(name: String): EitherNec[String, Model] = ???
  def validateDescription(text: String): EitherNec[String, Description] = ???
  def validateMakeModel(make: Make, model: Model): EitherNec[String, MakeModel] = ???

  /**
   * @see https://timushev.com/posts/2019/09/24/either-validated-parallel/
   */
  def validate(makeName: String,
               modelName: String,
               descriptionText: String): EitherNec[String, Listing] = {
    import cats.implicits._
    val make        = validateMake(makeName)
    val model       = validateModel(modelName)
    val description = validateDescription(descriptionText)

    val makeModel = (make, model).parTupled
      .flatMap { case (make, model) => validateMakeModel(make, model) }

    (makeModel, description).parMapN(Listing)
  }
}
