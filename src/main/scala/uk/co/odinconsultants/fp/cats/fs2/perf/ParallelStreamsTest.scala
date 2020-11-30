package uk.co.odinconsultants.fp.cats.fs2.perf

import cats.collections.Heap
import cats.effect._
import cats.implicits._
import fs2._

// Run this with a 15MB heap. It will time-out on scastie so it needs to be run elsewhere.
/**
 * mn98 @mn98 Nov 24 19:38
 * I'm not suspicious of the merging at this point, because the single streams parJoined or the merged stream succeed on their own without any apparent issues.
 * Simplifying it slightly, I have two streams, a and b, plus a third stream c = f(a, b). What I'd like to do is run all three in parallel, but perhaps it's not possible.
 * I can run Stream(a, b).parJoinUnbounded or c.
 * but the example above kind of suggests that in simple cases it should work.
 * And I can't help thinking it's because in my real world use case I am pulling a and b from disk, and perhaps I'm not doing that carefully enough. But this is uneducated guess work on my part!
 * Fabio Labella @SystemFw Nov 24 19:45
 * , a and b, plus a third stream c = f(a, b)
 *
 * well, that doesn't compile, and the semantics are complicated enough that I can't just infer them
 *
 * does f combine the elements of the stream pairwise?
 * also is each of a and b running twice?
 * one on its own, and the other to feed f?
 * or do you want to only run a and b once?
 * mn98 @mn98 Nov 24 20:03
 * a and b are running twice, which is why I'd started to get some kind of broadcast going.
 * because I think they should only be running once
 * I think perhaps I should abandon this joining and run them as two independent programs
 *
 * @Daenyth @SystemFw I dug further into my 'deadlock' and it turns out I'm running out of heap. I increased the heap and it took longer to grind to a halt. I then tested Stream(streams: _*).parJoinUnbounded vs SortMerge.sortMerge(streams). The former works fine while the latter gradually runs out of heap. @johnynek helpfully shared the SortMerge gist on this forum a while back and it works great apart from the heap issue.
 *          Is this behaviour expected? Any thoughts on why the sortMerge gradually eats up the heap?
 */
object ParallelStreamsTest extends IOApp {

  def sourceStreams: Seq[Stream[IO, Int]] =
    Seq.fill(6)(Stream.range(0, 10000).covary[IO])

  def f[A]: Pipe[IO, A, Unit] = _.evalMap(i => IO(println(s"i = $i")))

  val program: Stream[IO, Unit] = {
    for {
      sources <- Stream.eval(IO(sourceStreams))
      _ <-
        //Stream(sources: _*).parJoinUnbounded // <--- this works
        sortMerge(sources.toList) // <--- this OOMs
          .through(f)
          .onFinalize(IO(println("Exhausted!")))
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)

  def sortMerge[F[_], A: Ordering](streams: List[Stream[F, A]]): Stream[F, A] = {

    implicit val ord: cats.Order[Stream.StepLeg[F, A]] =
      new cats.Order[Stream.StepLeg[F, A]] {
        val ordA: Ordering[A] = implicitly[Ordering[A]]

        def compare(left: Stream.StepLeg[F, A], right: Stream.StepLeg[F, A]): Int = {
          if (left.head.isEmpty) {
            // prefer to step so we don't skip items
            if (right.head.isEmpty) 0 else -1
          }
          else if (right.head.isEmpty) {
            // we need to step so we don't mis-order items
            1
          }
          else {
            // neither are empty just compare the head
            ordA.compare(left.head(0), right.head(0))
          }
        }
      }

    def go(heap: Heap[Stream.StepLeg[F, A]]): Pull[F, A, Unit] =
      heap.pop match {
        case Some((sl, rest)) =>
          if (sl.head.nonEmpty) {
            for {
              _ <- Pull.output1(sl.head(0))
              nextSl = sl.setHead(sl.head.drop(1))
              nextHeap = rest.add(nextSl)
              _ <- go(nextHeap)
            } yield ()
          }
          else {
            // this chunk is done
            sl.stepLeg
              .flatMap {
                case Some(nextSl) =>
                  val nextHeap = rest.add(nextSl)
                  go(nextHeap)
                case None =>
                  // this leg is exhausted
                  go(rest)
              }
          }

        case None => Pull.done
      }

    val heap: Pull[F, fs2.INothing, Heap[Stream.StepLeg[F, A]]] =
      streams
        .traverse(_.pull.stepLeg)
        .map { ls => Heap.fromIterable(ls.flatten) }

    heap.flatMap(go).stream
  }

}
