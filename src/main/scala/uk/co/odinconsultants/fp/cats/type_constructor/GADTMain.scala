package uk.co.odinconsultants.fp.cats.type_constructor

/**
 * @see https://medium.com/disney-streaming/fix-point-type-for-gadt-scala-dc4e2cde349b
 */
object GADTMain {

//  sealed trait Query[A]
  sealed trait QueryF[+F[_], A]
  case object QueryStringF extends QueryF[Nothing, String]
  case object QueryBoolF extends QueryF[Nothing, Boolean]
  case class QueryPathF[F[_], A](path: String, next: F[A]) extends QueryF[F, A]

  case class HFix[F[_[_], _], A](unfix: F[HFix[F, ?], A])

  def queryString = HFix(QueryStringF: QueryF[HFix[QueryF,?], String])
  def queryBool = HFix(QueryBoolF: QueryF[HFix[QueryF,?], Boolean])
  def queryPath[A](p: String, next: HFix[QueryF, A]) = HFix(QueryPathF(p, HFix(next.unfix)))

  import cats.~>

  trait HFunctor[F[_[_], _]] {
    def hmap[I[_], J[_]](nt: I ~> J): F[I, ?] ~> F[J, ?]
  }

  implicit val queryHFunctor: HFunctor[QueryF] = new HFunctor[QueryF] {
    def hmap[I[_], J[_]](nt: I ~> J): QueryF[I, ?] ~> QueryF[J, ?] = {
      new (QueryF[I, ?] ~> QueryF[J, ?]) {
        def apply[A](a: QueryF[I, A]): QueryF[J, A] = {
          a match {
            case QueryStringF            => QueryStringF
            case QueryBoolF              => QueryBoolF
            case query: QueryPathF[I, A] => QueryPathF(query.path, nt(query.next))
          }
        }
      }
    }
  }

  type HAlgebra[F[_[_], _], G[_]] = F[G, ?] ~> G
  def hCata[F[_[_], _], G[_], I](alg: HAlgebra[F, G],hfix: HFix[F, I])(implicit F: HFunctor[F]): G[I] = {
    val inner = hfix.unfix
    val nt = F.hmap(
      new (HFix[F, ?] ~> G) {
        def apply[A](fa: HFix[F, A]): G[A] = hCata(alg, fa)
      }
    )(inner)
    alg(nt)
  }

  type JustString[A] = String
  // important part: convert each layer of query into a string
  val print: HAlgebra[QueryF, JustString] = new HAlgebra[QueryF, JustString] {
    override def apply[A](fa: QueryF[JustString, A]): JustString[A] = {
      fa match {
        case QueryStringF                 => "as[String]"
        case QueryBoolF                   => "as[Bool]"
        case q: QueryPathF[JustString, A] => s"${q.path}.${q.next}"
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val nestedQuery = queryPath(
      "oh",
      queryPath(
        "my",
        queryString
      )
    )
    println(hCata(print, nestedQuery))
  }
}
