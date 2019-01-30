package uk.co.odinconsultants.fp.cats.effects

import java.io.File

import cats.effect._
import cats.implicits._

import scala.io.Source

/**
  * From Fabio Labella on Gitter
  */
object ReadLinesFromFile {

  def addPlus(x: String): String = x + "+"

  def readFile[F[_]: Sync](file: File): F[List[String]] =
    Resource.fromAutoCloseable{
      Sync[F].delay(Source.fromFile(file))
    }.use { src =>
      Sync[F]
        .delay(src.getLines)
        .map(_.toList.map(addPlus))
    }
}
