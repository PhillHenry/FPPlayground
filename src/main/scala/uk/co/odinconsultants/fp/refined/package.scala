package uk.co.odinconsultants.fp

import eu.timepit.refined.api.Refined
import eu.timepit.refined.generic.Equal

package object refined {

  type ExactInt = Int Refined Equal[A] forSome { type A <: Int }

}
