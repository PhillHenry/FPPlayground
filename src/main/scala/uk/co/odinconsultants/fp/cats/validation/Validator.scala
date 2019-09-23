package uk.co.odinconsultants.fp.cats.validation

import cats._
import cats.implicits._

class Validator[T[_]: Monad, W](T1: T[W], T2: T[W], T3: T[W]) {

  def getT: T[W] = T1
  def getU: T[W] = T2
  def getV: T[W] = T3

  def collect(w1: W, w2: W, w3: W): String = List(w1, w2, w3).mkString(" ")

  def doMonads: T[String] = for {
    a <- getT
    b <- getU
    c <- getV
  } yield collect(a, b, c)

  def doApplicatives: T[String] = (T1, T2, T3).mapN { case (x, y, z) => collect(x, y, z) }

}

