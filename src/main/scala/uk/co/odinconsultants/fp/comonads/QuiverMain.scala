package uk.co.odinconsultants.fp.comonads

import quiver.GDecomp
import quiver.viz._
import quiver._


/**
 * See http://blog.higher-order.com/blog/2016/04/02/a-comonad-of-graph-decompositions/
 */
object QuiverMain {

//  class QuiverMain[V, E] {
//
//    def cobind[A, B](g: GDecomp[V, A, E])(
//      f: GDecomp[V, A, E] => B): GDecomp[V, A, E] =
//      GDecomp(g.ctx.copy(label = f(g)),
//        g.rest.decompAny.toGDecomp.map { x =>
//          val GDecomp(c, r) = cobind(x)(f)
//          c & r
//        } getOrElse empty)
//  }


  def makeNodes(n: Int): Seq[LNode[V, A]] = (0 until n).map(i => LNode(s"v$i", A(s"node label $i")))

  def chain(ns: Seq[LNode[V, A]]): Seq[LEdge[V, B]] = ns.tail.map { n => LEdge(n.vertex, ns.head.vertex, B("relationship")) }

  type V = String
  type E = Int

  case class A(x: String)
  case class B(x: String)

  def main(args: Array[String]): Unit = {

    val nodes: Seq[LNode[V, A]] = makeNodes(5)
    val edges: Seq[LEdge[V, B]] = chain(nodes)

    val g: Graph[V, A, B] = mkGraph(nodes, edges)
    val decomposed: Decomp[V, A, B] = g.decompAny

    def contextAsString(decomposed: Decomp[V, A, B]): String = decomposed.ctx match {
      case None => "no decomposition"
      case Some(ctx) =>
        s"""in      = ${ctx.inAdj}
           |out     = ${ctx.outAdj}
           |label   = ${ctx.label}
           |vertex  = ${ctx.vertex}""".stripMargin
    }


    println(s"decomposed\nContext:\n${contextAsString(decomposed)}\n\nRest:\n${decomposed.rest}")
//    val GDecomp(c, r) = main.cobind(_)(f)
//    c & r
  }
}
