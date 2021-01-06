package uk.co.odinconsultants.fp.zio.errors

import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, testM, _}
import zio.{Cause, IO, ZIO}

/**
 * @ghostdogpr 23/6/2020 (Discord)
 * @Igosuki .catchAll if you want to catch the error and return a proper value, orDie if you want to terminate
 *         the fiber instead
 *
 */
object ErrorChannelSpec extends DefaultRunnableSpec {

  val e = new Exception()

  val badBoy = ZIO {
    throw e
  }

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("failed ZIOs") (
      testM("map to Nones") {
        assertM(badBoy.option)(equalTo(None))
      }
      ,
      testM("map to Lefts") {
        assertM(badBoy.either)(equalTo(Left(e)))
      }
      ,
      testM("catchAll") {
        assertM(badBoy.catchAll(x => ZIO(x)))(equalTo(e))
      }
      ,
// Can't even @@ignored this:
// Error:(38, 12) type mismatch;
// found   : zio.ZIO[Any,Nothing,zio.test.TestSuccess]
// required: zio.test.TestAspect[?,zio.test.environment.TestEnvironment,?,?]
//    (which expands to)  zio.test.TestAspect[?,zio.Has[zio.clock.Clock.Service] with zio.Has[zio.console.Console.Service] with zio.Has[zio.system.System.Service] with zio.Has[zio.random.Random.Service] with zio.Has[zio.blocking.Blocking.Service] with zio.Has[zio.test.Annotations.Service] with zio.Has[zio.test.environment.TestClock.Service] with zio.Has[zio.test.environment.TestConsole.Service] with zio.Has[zio.test.environment.Live.Service] with zio.Has[zio.test.environment.TestRandom.Service] with zio.Has[zio.test.Sized.Service] with zio.Has[zio.test.environment.TestSystem.Service],?,?]
//      } @@ ignored // "Fiber failed. An unchecked error was produced."

//      testM("orDie") {
//        assertM(badBoy.orDie)(equalTo(e))
//      } @@ ignored // "Fiber failed. An unchecked error was produced."
//      ,
      testM("orDie") {
        // Error:(48, 28) This error handling operation assumes your effect can fail. However, your effect has
        // Nothing for the error type, which means it cannot fail, so there is no need to handle the failure.
        // To find out which method you can use instead of this operation, please see the reference chart at: https://zio.dev/docs/canfail
        //        assertM(badBoy.run.orDie)(equalTo(e))
//        assertM(badBoy.run.orDie)(equalTo(e))

        //Failure(Traced(Die(java.lang.Exception),ZTrace(Id(1593080584987,17),List(SourceLocation(ZIO.scala,zio.ZIO,orDieWith,969), SourceLocation(Predef.scala,scala.Predef$$anon$2,apply,504), SourceLocation(ErrorChannelSpec.scala,uk.co.odinconsultants.fp.zio.errors.ErrorChannelSpec$,badBoy,19), SourceLocation(ZIO.scala,zio.ZIO$,effectSuspendTotal,2260)),List(SourceLocation(ZIO.scala,zio.ZIO,run,1450), SourceLocation(package.scala,zio.test.package$,assertM,164), SourceLocation(package.scala,zio.test.package$ZTest$,apply,104)),Some(ZTrace(Id(1593080584942,6),List(SourceLocation(ZIO.scala,zio.ZIO$BracketRelease_,apply,3310), SourceLocation(FiberRef.scala,zio.FiberRef,locally,91), SourceLocation(package.scala,zio.test.package$Annotations$,withAnnotation,515), SourceLocation(ZIO.scala,zio.ZIO,bracket_,244), SourceLocation(FiberContext.scala,zio.internal.FiberContext,evaluateNow,523), SourceLocation(ZIO.scala,zio.ZIO,provideLayer,1042), SourceLocation(Predef.scala,scala.Predef$$anon$2,apply,504), SourceLocation(FiberContext.scala,zio.internal.FiberContext$InterruptExit$,apply,155), SourceLocation(FiberContext.scala,zio.internal.FiberContext$InterruptExit$,apply,148), SourceLocation(FiberContext.scala,zio.internal.FiberContext$InterruptExit$,apply,148)),List(SourceLocation(package.scala,zio.test.package$Annotations$$anon$1,withAnnotation,504), SourceLocation(ZIO.scala,zio.ZIO,run,1450), SourceLocation(ZIO.scala,zio.ZIO$BracketRelease_,apply,3310), SourceLocation(FiberRef.scala,zio.FiberRef,locally,92), SourceLocation(ZIO.scala,zio.ZIO,run,1450), SourceLocation(ZIO.scala,zio.ZIO,bracket_,244), SourceLocation(ZIO.scala,zio.ZIO,run,1450), SourceLocation(ZManaged.scala,zio.ZManaged,use,904), SourceLocation(Spec.scala,zio.test.Spec,foreachExec,235), SourceLocation(ZIO.scala,zio.ZIO$,foreachParN,2533)),Some(ZTrace(Id(1593080584118,0),List(SourceLocation(ZIO.scala,zio.ZIO,zipWith,1831), SourceLocation(ZIO.scala,zio.ZIO$,foreachParN,2532), SourceLocation(ZIO.scala,zio.ZIO$,foreachParN,2531), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO,zipWith,1831)),List(SourceLocation(ZIO.scala,zio.ZIO,zipWith,1831), SourceLocation(ZIO.scala,zio.ZIO$,foreach,2364), SourceLocation(ZIO.scala,zio.ZIO$,foreachParN,2535), SourceLocation(ZIO.scala,zio.ZIO,run,1450), SourceLocation(ZIO.scala,zio.ZIO$,foreachParN,2528), SourceLocation(ZIO.scala,zio.ZIO,refailWithTrace,1274), SourceLocation(Spec.scala,zio.test.Spec,foldM,197)),None))))))) did not satisfy fails(equalTo(Die(java.lang.Exception)))
        assertM(badBoy.orDie.run)(fails(equalTo(Cause.Die(e))))
      }
      ,
      testM("fails when given Some error") {
        val failing:  IO[Option[String],  Nothing]          = IO.fail(Some("Error")) // E must be Option[_] or .optional fails on the next line
        val task:     IO[String,          Option[Nothing]]  = failing.optional
        assertM(task.run)(fails(equalTo("Error")))
      }
    )
  }
}
