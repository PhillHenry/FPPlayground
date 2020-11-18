package uk.co.odinconsultants.fp.lang

import org.scalatest.{Matchers, WordSpec}

class JoyOfSetsSpec extends WordSpec with Matchers {

  import JoyOfSets._

  s"Contents of wrappers in  $set1 and $set2" should {
    "be equal" in {
      set1.map(_.x) shouldEqual set2.map(_.x)
    }
  }

  "Set.toList as String" should {
    "not necessarily be equal" in {
      set1AsString should not equal(set2AsString)
    }
  }

}
