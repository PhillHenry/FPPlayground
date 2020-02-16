package uk.co.odinconsultants.fp.refined

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric._
import shapeless.Witness
import shapeless.Witness.Aux

object SensiblePortMain {

  type Port = Int Refined Greater[W.`0`.T] //Refined Less[W.`65536`.T]

  def dummyListOn(port: Port): Unit = {
    println(s"Pretending to listen on port $port")
  }

  def main(args: Array[String]): Unit = {
//    dummyListOn(0: Port) // this fails to compile as expected
    dummyListOn(1: Port)
  }

}
