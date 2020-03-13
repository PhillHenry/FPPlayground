package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness
import shapeless.Witness.Aux
import eu.timepit.refined.api.RefType
import eu.timepit.refined.boolean._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.generic._
import eu.timepit.refined.string._
import shapeless.{ ::, HNil }

object SensiblePortMain {

  type Port = Int Refined Interval.ClosedOpen[W.`1`.T, W.`65535`.T]

  def dummyListOn(port: Port): Unit = {
    println(s"Pretending to listen on port $port")
  }

  def dummyListenOn(port: Port): Unit =
    println(s"Pretending to listen on port $port")

  def main(args: Array[String]): Unit = {
//    dummyListOn(0: Port)                                    // this fails to compile as expected
//    val _0: Refined[Int, Port] = refineMV[Port](0)          // this fails to compile as expected
//    val _65536: Refined[Int, Port] = refineMV[Port](65536)  // this fails to compile as expected
    dummyListenOn(1: Port)
//    dummyListenOn(0: Port)                                  // this fails to compile as expected
//    dummyListenOn(65536: Port)                              // this fails to compile as expected
//    dummyListenOn(refineMV
//    dummyListOn(refineMV[Port](1))

    type ZeroToOne = Not[Less[W.`0.0`.T]] And Not[Greater[W.`1.0`.T]]
    refineMV[ZeroToOne](0.8)

    val _08: Refined[Double, ZeroToOne] = refineMV[ZeroToOne](0.8)
  }

}
