package uk.co.odinconsultants.fp.cats.tagless

import cats.data.Const

trait KVStore[F[_]] {
  def get(key: String): F[Option[String]]
  def put(key: String, a: String): F[Unit]
}

/**
  * @see https://typelevel.org/blog/2017/12/27/optimizing-final-tagless.html
  */
object FarewellToFree extends App {

  import cats._
  import cats.implicits._

  def program[M[_]: FlatMap, F[_]](a: String)(K: KVStore[M])(implicit P: Parallel[M, F]) =
    for {
      _ <- K.put("A", a)
      x <- (K.get("B"), K.get("C")).parMapN(_ |+| _)
      _ <- K.put("X", x.getOrElse("-"))
    } yield x

  def program[F[_]: Apply](F: KVStore[F]): F[List[String]] =
    (F.get("Cats"), F.get("Dogs"), F.put("Mice", "42"), F.get("Cats"))
      .mapN((f, s, _, t) => List(f, s, t).flatten)

  val analysisInterpreter: KVStore[Const[(Set[String], Map[String, String]), ?]] =
    new KVStore[Const[(Set[String], Map[String, String]), ?]] {
      def get(key: String) = Const((Set(key), Map.empty))
      def put(key: String, a: String) = Const((Set.empty, Map(key -> a)))
    }

//  program(analysisInterpreter).getConst

}
