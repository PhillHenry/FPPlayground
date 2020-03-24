package uk.co.odinconsultants.fp.lang

object EvidencesMain {

  class MyParent {}

  class MySuper extends MyParent { }

  class MySub extends MySuper {}

  class MySubSub extends MySub {}

  def demandsProof[A](implicit a: A <:< MySuper): Unit = println(s"OK $a")

  def main(args: Array[String]): Unit = {
    demandsProof[MySub] // "OK <function1>"
  }

}
