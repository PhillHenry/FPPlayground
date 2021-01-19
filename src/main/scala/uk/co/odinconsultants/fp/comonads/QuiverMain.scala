package uk.co.odinconsultants.fp.comonads

import cats.Comonad
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

  def gdecompComonad[V,E] = new Comonad[λ[α => GDecomp[V,α,E]]] {

    def extract[A](g: GDecomp[V,A,E]): A = g.ctx.label

    def coflatMap[A,B](g: GDecomp[V,A,E])(f: GDecomp[V,A,E] => B): GDecomp[V,B,E] = GDecomp(g.ctx.copy(label = f(g)),
      g.rest.decompAny.toGDecomp.map { x =>
        val GDecomp(c, r) = coflatMap(x)(f)
        c & r
      } getOrElse empty)

    def map[A, B](fa: GDecomp[V, A, E])(f: A => B): GDecomp[V, B, E] = fa.map(f)
  }


  def main(args: Array[String]): Unit = {

    val nodes: Seq[LNode[V, A]] = makeNodes(5)
    val edges: Seq[LEdge[V, B]] = chain(nodes)

    val g:          Graph[V, A, B]  = mkGraph(nodes, edges)
    val decomposed: Decomp[V, A, B] = g.decompAny


    println(s"maxDegree = ${maxDegreePlain(g)}")

  }

  def contextGraph[V,N,E](g: Graph[V,N,E]): Graph[V,Context[V,N,E],E] = ???

  def maxDegree[V,N,E](g: Graph[V,N,E]): Int =
    contextGraph(g).fold(0) { (c, z) =>
      z max (c.label.ins.size + c.label.outs.size)
    }

  def maxDegreePlain(g: Graph[V, A, B]) = g.fold(0) { (c, z) =>
    g.decomp(c.vertex).toGDecomp.map {
      case GDecomp(Context(ins, _, _, outs), _) =>
        ins.size + outs.size
    }.getOrElse(0) max z
  }

  def contextAsString[V, A, B](ctx: Option[Context[V, A, B]]): String = ctx match {
    case None       => "no decomposition"
    case Some(ctx)  =>
      s"""in      = ${ctx.inAdj}
         |out     = ${ctx.outAdj}
         |label   = ${ctx.label}
         |vertex  = ${ctx.vertex}""".stripMargin
  }
}
