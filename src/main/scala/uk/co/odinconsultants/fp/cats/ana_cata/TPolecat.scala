package uk.co.odinconsultants.fp.cats.ana_cata

/**
 * Anthony Cerruti @srnb_gitlab Apr 22 05:51
 * If I have a tree:
 *
 * Tree("A", Vector(
 *  Tree("B", Vector(
 *    Tree("C", Vector(
 *      Tree("A", Vector())
 *    )
 *  ),
 *  Tree("C", Vector(
 *    Tree("A", Vector())
 *   )
 * )
 * I want to take equivalent sub-trees (There's one here, "C" -> "A") and turn that into something like this:
 *
 * Compressed(
 *  root = Right(Tree("A", Vector(
 *    Right(Tree("B", Vector(Left(1)))),
 *    Left(1)
 *  ),
 *  subTrees = Map(
 *    1 -> Tree("C", Vector(
 *      Right(Tree("A", Vector()))
 *    )
 *  )
 * )
 * How solvable is this?
 *
 * I'd like an algorithm and not a heuristic. I'm running it on a ton of data as well, so if there are Big Data:tm: libraries that'll help me with it, I'm extremely interested.
 *
 *
 * Rob Norris @tpolecat Apr 22 16:20
 * srnb_gitlab here's a solution using Cofree https://gist.github.com/tpolecat/5c86f1ceba7ef50e12f229144b1f07ee
 * never pass up an opportunity to coflatMap that shit
 * I included an image with worksheet output … I don't know how to copy/paste the output as text
 *
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

  println(show(tree))

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
  println(s"tree.size = ${tree.size}")

  // Count of nodes in table
  println(s"flat.table.size = ${flat.table.size}")
  println(s"flat = $flat")

  // Unfold back into a tree
  val treeʹ: Tree[String] =
    Cofree.ana(flat.root)(
      a => flat.table(a)._2,
      a => flat.table(a)._1
    )

  println(s"tree  = ${show(tree )}")
  println(s"tree' = ${show(treeʹ)}")
}
