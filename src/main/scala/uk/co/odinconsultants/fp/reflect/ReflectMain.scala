package uk.co.odinconsultants.fp.reflect

import shapeless.Witness

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

  import izumi.reflect.Tags.Tag
  import izumi.reflect.Tags.TagK
  import izumi.reflect.macrortti.{LTag, LightTypeTag}

  def tagOf[T: LTag]: LightTypeTag = {
    val x: LightTypeTag = LTag[T].tag

    val args: List[LightTypeTag] = x.typeArgs
    println("tagOf args = " + args.mkString(","))

    x
  }

  val w3 = Witness(3)
  type _3 = w3.T

  def main(args: Array[String]): Unit = {
    println("ClassTags:")
    classTagOf[List[Int]] // List
    println("\n\nTypeTags:")
    typeTagOf[List[Int]] // Int

    println("\nRefined:")

    typeTagOf[List[_3]] // "No TypeTag available for List[_3]" if _3 is scoped to the fucntion

    println("tagOf = " + tagOf[List[_3]])
  }

}
