package uk.co.odinconsultants.fp.cats.effects

import java.io.{BufferedReader, File, FileReader}

import cats.effect._
import cats.implicits._

/**
  * Adapted rom Fabio Labella on Gitter
  */
object ReadLinesFromFile {

  def addPlus(x: String): String = x + "+"

  def readLines(src: BufferedReader): List[String] =
    src.lines().toArray().toList.map(_.toString)

  def readFile[F[_]: Sync](file: File): F[List[String]] =
    Resource.fromAutoCloseable{
      Sync[F].delay(new BufferedReader(new FileReader(file)))
    }.use { src =>
      Sync[F]
        .delay(readLines(src))
        .map(_.map(x => addPlus(x)))
    }
}
