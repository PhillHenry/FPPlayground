package uk.co.odinconsultants.fp.cats.monads

import scala.util.Try

object MixedForComprehension {

  def anOption(x: String): Try[Option[String]] = Try { Option(x) }

  def aTry(x: => String): Try[String] = Try(x)

  def main(args: Array[String]): Unit = {
    // code inspired by the SO question but it seems a bit pointless as the monads are already homogeneous
    import cats.data._
    import cats.implicits._
    val result = for {
      o <- OptionT(anOption("test"))
      t <- OptionT.liftF(aTry(o))
    } yield {
      t
    }
    println(s"result = $result")
  }

  /**
   * @see https://stackoverflow.com/questions/52897884/composing-multiple-different-monad-types-in-a-for-comprehension
   */
  def fromSO(): Unit = {

    case class MovieTicketSale(id: Int)
    case class DBIO()
    case class UUID()

    import cats.data._
    import cats.implicits._

    val movieTicketSaleNumbers: List[MovieTicketSale] = List(MovieTicketSale(1))
    def findOneExact(id: Int): Try[Option[Int]] = ???
    def insertMovieTicketSale(sale: MovieTicketSale, id: Int): Try[UUID] = ???

    def insertTicket(sale: MovieTicketSale) =
      for {
        movie <- OptionT(findOneExact(sale.id))
        movieNumberDbId <- OptionT.liftF(insertMovieTicketSale(sale, movie))
      } yield movieNumberDbId
  }

}
