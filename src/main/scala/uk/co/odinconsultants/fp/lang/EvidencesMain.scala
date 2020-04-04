package uk.co.odinconsultants.fp.lang

import uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated

/**
 *Rob Norris @tpolecat Apr 02 22:26
*A <:< B extends A => B and is available implicitly if A <: B
*A =:= B extends A => B and is available implicitly if A is the same type as B
 *
 */
object EvidencesMain {

  class MyParent {}

  class MySuper extends MyParent { }

  class MySub extends MySuper {}

  class MySubSub extends MySub {}

  class Unrelated

  implicit def unrelatedToMySuper: Unrelated => MySuper = { _ => new MySuper }
  implicit val unrelatedToMySuperFn: Unrelated => MySuper = { _ => new MySuper }

  implicit class MySuperOps(x: Unrelated) {
    def unrelatedToMySuper: MySuper = new MySuper
  }

  implicit object MySuperOps {
    def unrelatedToMySuper(x: Unrelated): MySuper = new MySuper
  }

  def demandsProof[A](implicit a: A <:< MySuper): Unit = println(s"OK $a")
  def demandsProofSuper[A](implicit a: MySuper <:< A): Unit = println(s"OK $a")
  def demandsWitness[A](implicit a: A =:= MySuper): Unit = println(s"OK $a")

  def main(args: Array[String]): Unit = {
    demandsProof[MySub]     // "OK <function1>"
    demandsProof[MySubSub]  // "OK <function1>"
    demandsProof[MySuper]   // "OK <function1>"
//    demandsWitness[MySub]
//    demandsWitness[MySubSub]
    demandsWitness[MySuper]   // "OK <function1>"
//    demandsProof[MyParent]  //"Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper."
//    demandsProof[Unrelated] // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper."
//    demandsProofSuper[Unrelated] // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper <:< uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated.
//    demandsProofSuper[Unrelated] // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper."
//    demandsWitness[Unrelated] // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated =:= uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper.

    /*
found   : uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated => uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper
required: uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper
demandsProof[Unrelated](unrelatedToMySuperFn) // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper."
     */
//    demandsProof[Unrelated](unrelatedToMySuperFn) // "Cannot prove that uk.co.odinconsultants.fp.lang.EvidencesMain.Unrelated <:< uk.co.odinconsultants.fp.lang.EvidencesMain.MySuper."
  }

}
