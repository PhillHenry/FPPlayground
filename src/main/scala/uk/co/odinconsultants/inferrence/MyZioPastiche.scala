package uk.co.odinconsultants.inferrence

import izumi.reflect.Tags.Tag

import scala.util.control.NoStackTrace

case class UserId(x: Int)

case class User(id: UserId, name: String)

class DBError extends NoStackTrace

object MyZioPastiche {

  def main(args: Array[String]): Unit = {
    println(myGetUser(UserId(1)))
  }

  type IO[T, U] = MyZio[Any, T, U]

  import MyTypeInference._

  type UserRepo = MyDesugaredHas[UserRepo.Service]

  object UserRepo {
    trait Service {
      def getUser(userId: UserId): IO[DBError, Option[User]]
    }
  }

  def myGetUser(userId: UserId): MyRead[UserRepo, DBError, Option[User]] = {
    MyTypeInference.accessM(_.get.getUser(userId))
  }

}

class MyZio[-R, +E, +A]

final class MyDesugaredHas[A]

object MyDesugaredHas {
  type Tagged[A] = Tag[A]
  implicit final class MyDesugaredHasSyntax[Self <: MyDesugaredHas[_]](private val self: Self) extends AnyVal {
    def get[B](implicit ev: Self <:< MyDesugaredHas[_ <: B], tagged: Tagged[B]): B = ???
  }
}

object MyTypeInference {

  final class MyRead[R, E, A](val k: R => MyZio[R, E, A]) {
    def tag = 13
  }

  def accessM[R]: MyAccessMPartiallyApplied[R] =
    new MyAccessMPartiallyApplied[R]

  final class MyAccessMPartiallyApplied[R](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](f: R => MyZio[R, E, A]): MyRead[R, E, A] =
      new MyRead(f)
  }

}
