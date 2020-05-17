package uk.co.odinconsultants.misc

import java.math.{MathContext, RoundingMode, BigDecimal => BigDec}

import math._

object CollisionsMain {

  def main(args: Array[String]): Unit = {
    val mathContext                 = MathContext.DECIMAL128
//    val mathContext                 = new MathContext(pow(2, 16).toInt)
    val _1N                         = new BigDec(pow(2, -128), mathContext)
    val _1                          = new BigDec(1, mathContext)
    val b: Int                      = 2e7.toInt
    val p_noCollision               = _1.subtract(_1N)
    println(s"p(no collision)       = $p_noCollision")
    val p_noCollisions              = p_noCollision.pow(b, mathContext)
    println(s"p(no collisions)      = $p_noCollisions")
    val p_collision                 = new BigDec(1, mathContext).subtract(p_noCollisions)
    println(s"p(collision)          = ${p_collision}")
    println(s"E[collisions]         = ${p_collision.multiply(new BigDec(b))}")

    val charSpace: BigInt   = 52
    val nChars              = 7
    println(s"p(guessing a 10 character password) = ${charSpace.pow(nChars)}")

    println(2e64)

  }

}
