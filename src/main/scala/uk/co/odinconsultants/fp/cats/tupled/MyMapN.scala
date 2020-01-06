package uk.co.odinconsultants.fp.cats.tupled

import cats.implicits._

object MyMapN {

  type MyEffect[T] = List[T]

  def myMapN(x: MyEffect[Int], y: MyEffect[Int]): MyEffect[Int] = {
    (x, y).mapN { (i, j) => i + j }
  }

}
