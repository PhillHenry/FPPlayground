package uk.co.odinconsultants.fp.zio.modules

import org.scalatest.{Matchers, WordSpec}
import zio.ZIO

class MyTypeInference extends WordSpec with Matchers {

  import MyLayers._
  import MyTypeInference._

  def getUser(userId: UserId): ZIO[UserRepo, DBError, Option[User]] =
    ZIO.accessM(_.get.getUser(userId))

  def myGetUser(userId: UserId): MyRead[UserRepo, DBError, Option[User]] = {
    MyTypeInference.accessM(_.get.getUser(userId))
  }

  "Argument" should {
    "be inferred from return type in ZIO" in {
      println(getUser(UserId(1)))
    }
    "be inferred in my code" in {
      println(myGetUser(UserId(1)))
    }
  }

}

object MyTypeInference {

  final class MyRead[R, E, A](val k: R => ZIO[R, E, A]) {
    def tag = 13
  }

  def accessM[R]: MyAccessMPartiallyApplied[R] =
    new MyAccessMPartiallyApplied[R]

  final class MyAccessMPartiallyApplied[R](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](f: R => ZIO[R, E, A]): MyRead[R, E, A] =
      new MyRead(f)
  }

}
