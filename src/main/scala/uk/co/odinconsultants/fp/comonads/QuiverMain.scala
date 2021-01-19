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

    val g:          Graph[V, A, B]  = mkGraph(nodes, edges)
    val decomposed: Decomp[V, A, B] = g.decompAny

    def contextAsString(ctx: Option[Context[V, A, B]]): String = ctx match {
      case None       => "no decomposition"
      case Some(ctx)  =>
        s"""in      = ${ctx.inAdj}
           |out     = ${ctx.outAdj}
           |label   = ${ctx.label}
           |vertex  = ${ctx.vertex}""".stripMargin
    }

    val gDecomp: Option[GDecomp[V, A, B]] = decomposed.toGDecomp
    val gDecompStr = gDecomp match {
      case Some(x)  => s"${contextAsString(decomposed.ctx)}\n\nRest:\n${x.rest}"
      case None     => "no toGDecomp"
    }

    val maxDegree = g.fold(0) { (c, z) =>
      g.decomp(nodes.head.vertex).toGDecomp.map {
        case GDecomp(Context(ins, _, _, outs), _) =>
          ins.size + outs.size
      }.getOrElse(0) max z
    }
    println(s"maxDegree = $maxDegree")

//    println(s"toGDecomp:\n$gDecompStr")
//    println()
//    println(s"decompAny\nContext:\n${contextAsString(decomposed.ctx)}\n\nRest:\n${decomposed.rest}")
//    val GDecomp(c, r) = main.cobind(_)(f)
//    c & r
  }
}
