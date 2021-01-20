package uk.co.odinconsultants.fp.comonads

import cats.Comonad
import quiver.GDecomp
import quiver.viz._
import quiver._


/**
 * See http://blog.higher-order.com/blog/2016/04/02/a-comonad-of-graph-decompositions/
 */
object QuiverMain {

  def makeNodes(n: Int): Seq[LNode[V, AA]] = (0 until n).map(i => LNode(s"v$i", AA(s"node label $i")))

  def chain(ns: Seq[LNode[V, AA]]): Seq[LEdge[V, BB]] = ns.zipWithIndex.tail.map { case(n, i) => LEdge(n.vertex, ns.head.vertex, BB(s"relationship $i")) }

  type V = String
  type E = Int

  case class AA(x: String)
  case class BB(x: String)

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
    val nodes: Seq[LNode[V, AA]] = makeNodes(5)
    val edges: Seq[LEdge[V, BB]] = chain(nodes)
    val g:     Graph[V, AA, BB]  = mkGraph(nodes, edges)

    examineSubGraph(nodes.head.vertex, g)

    println(s"maxDegree = ${maxDegreePlain(g)}")
    println(s"maxDegree = ${maxDegree(g)}")

  }

  private def examineSubGraph[V, A, B](vertex: V, graph: Graph[V, A, B]) = {
    val decomp: Decomp[V, A, B] = graph.decomp(vertex)
    val maybeSubGraph: Option[GDecomp[V, Context[V, A, B], B]] = decomp.toGDecomp.map { gDecomp =>
      gdecompComonad.coflatMap(gDecomp)(_.ctx)
    }
    val subGraph: GDecomp[V, Context[V, A, B], B] = maybeSubGraph.get
    println(s"subGraphs of ${gDecompAsString(subGraph)}\n")
    subGraph
  }

  def gDecompAsString[V, A, B](x: GDecomp[V, A, B]): String = {
    s"GDecomp\n${contextAsString(x.ctx)}\nRest:\n${x.rest}"
  }

  def contextGraph[V,N,E](g: Graph[V,N,E]): Graph[V,Context[V,N,E],E] = g.decompAny.toGDecomp.map { gDecomp =>
    gdecompComonad.coflatMap(gDecomp)(_.ctx)
  }.get.toGraph

  def maxDegree[V,N,E](g: Graph[V,N,E]): Int =
    contextGraph(g).fold(0) { (c, z) =>
      z max (c.label.ins.size + c.label.outs.size)
    }

  def maxDegreePlain(g: Graph[V, AA, BB]): Int = g.fold(0) { (context, acc) =>
    g.decomp(context.vertex).toGDecomp.map {
      case GDecomp(Context(ins, v, l, outs), _) =>
        println(s"maxDegreePlain visiting $v of decomposed ${context.vertex}. ${ins.size} incoming, ${outs.size} outgoing")
        ins.size + outs.size
    }.getOrElse(0) max acc
  }

  def contextOptionAsString[V, A, B](ctx: Option[Context[V, A, B]]): String = ctx match {
    case None       => "no decomposition"
    case Some(ctx)  => contextAsString(ctx)
  }

  def contextAsString[B, A, V](ctx: Context[V, A, B]) = {
    s"""in      = ${ctx.inAdj}
       |out     = ${ctx.outAdj}
       |label   = ${ctx.label}
       |vertex  = ${ctx.vertex}""".stripMargin
  }
}
