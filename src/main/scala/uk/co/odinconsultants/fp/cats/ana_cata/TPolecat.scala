package uk.co.odinconsultants.fp.cats.ana_cata

/**
 * Rob Norris @tpolecat Apr 22 20:18
 * Honestly ana/cata on fix, free, and cofree are all I ever use.
 */
object TPolecat extends App {
  import scala._, scala.Predef._
  import cats._
  import cats.free.Cofree
  import cats.implicits._

  // Some helpers
  implicit class CofreeOps[F[_]: Traverse, A](self: Cofree[F, A]) {
    def cata[B](f: (A, F[B]) => Eval[B]): Eval[B] = Cofree.cata(self)(f)
    def eagerCata[B](f: (A, F[B]) => B): B = cata[B]((a, fb) => Eval.now(f(a, fb))).value
  }

  // A rose tree
  type Tree[A] = Cofree[Vector, A]

  // Convenience constructor
  object Tree {
    def apply[A](head: A, tail: Tree[A]*): Tree[A] =
      Cofree(head, Eval.now(Vector(tail: _*)))
  }

  // Render a tree as a string
  def show[A](tree: Tree[A]): String =
    tree.eagerCata[String]((a, vb) => s"$a -> ${vb.mkString("{", ",", "}")}")

  // Our tree
  val tree: Tree[String] =
    Tree("A",
      Tree("B",
        Tree("C",
          Tree("A")
        ),
        Tree("A"),
        Tree("B")
      ),
      Tree("C",
        Tree("A")
      )
    )

  show(tree)

  // A flat representation
  case class Flat[A](root: Int, table: Map[Int, (A, Vector[Int])])

  // Fold a Tree into a Flat
  val flat: Flat[String] =
    tree.eagerCata[Flat[String]] { (s, vf) =>
      val root  = s.hashCode ^ vf.map(_.root).hashCode
      val table = vf.foldMapK(_.table) + (root -> (s, vf.map(_.root)))
      Flat(root, table)
    }

  // Count of nodes in tree
  tree.size

  // Count of nodes in table
  flat.table.size

  // Unfold back into a tree
  val treeʹ: Tree[String] =
    Cofree.ana(flat.root)(
      a => flat.table(a)._2,
      a => flat.table(a)._1
    )

  show(tree )
  show(treeʹ)
}
