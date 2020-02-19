package uk.co.odinconsultants.fp.reflect

import scala.reflect.ClassTag
import scala.reflect._
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

/**
 * @see https://docs.scala-lang.org/overviews/reflection/typetags-manifests.html
 */
object ReflectMain {

  def typeTagOf[X: TypeTag]: universe.Type = {
    val ttX: universe.Type = typeOf[X]
    println("ttX = " + ttX)
    println("typeTagOf type args: " + ttX.typeArgs.map(_.getClass).mkString(", "))
    println("typeTagOf type args: " + ttX.typeArgs.mkString(", "))
    ttX
  }

  def classTagOf[X: ClassTag]: ClassTag[X] = {
    val ctX: ClassTag[X] = classTag[X]
    println("ctX = " + ctX)
    val clazz: Class[_] = ctX.runtimeClass
    import scala.collection.JavaConverters._
    println(clazz.getTypeParameters.mkString(", "))
    println(clazz)
    ctX
  }

  def main(args: Array[String]): Unit = {
    println("ClassTags:")
    classTagOf[List[Int]] // List
    println("\n\nTypeTags:")
    typeTagOf[List[Int]] // Int
  }

}
