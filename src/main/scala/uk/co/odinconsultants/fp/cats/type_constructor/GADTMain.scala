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

  def main(args: Array[String]): Unit = {
    println("works")
  }
}
