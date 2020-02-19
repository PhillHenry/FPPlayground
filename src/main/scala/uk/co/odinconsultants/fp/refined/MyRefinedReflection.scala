package uk.co.odinconsultants.fp.refined

import eu.timepit.refined.W
import uk.co.odinconsultants.fp.refined.RefinedComparisonMain.Exactly
import uk.co.odinconsultants.fp.reflect.ReflectMain
import uk.co.odinconsultants.fp.reflect.ReflectMain._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe

object MyRefinedReflection {

  def main(args: Array[String]): Unit = {
    val ct: ClassTag[Exactly[W.`5`.T]] = classTagOf[Exactly[W.`5`.T]]
    println(ct)
    import scala.collection.JavaConverters
    println(ct.runtimeClass.getTypeParameters.mkString(", "))

    val tt: universe.Type = typeTagOf[Exactly[W.`5`.T]]
    println("typeArgs         = " + tt.typeArgs.mkString(", "))
    println("typeArgs classes = " + tt.typeArgs.map(_.getClass).mkString(", "))
    println("typeConstructor  = " + tt.typeConstructor)
  }

}
