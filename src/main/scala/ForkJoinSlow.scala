trait SumExecutor {
  val length = 10000000
  val list = (for (i <- 1 to length) yield i.toLong).toList

  protected def run(f: () => BigInt) {
    val start = System.currentTimeMillis()
    val sum = f()
    val time = System.currentTimeMillis() - start

    println(s"sum = ${sum}")
    println(s"time = ${time} msec") // 時間計測は JIT コンパイルや GC の影響を受けるため専用のライブラリを使って行うべき
  }
}

object SumTrial extends App with SumExecutor {
  run(() => list.sum)
}


object ForkJoinSumStudy extends App with SumExecutor {

  import java.util.concurrent.{ForkJoinPool, RecursiveTask}

  val pool = new ForkJoinPool()

  class AggregateTask(list: List[Long]) extends RecursiveTask[Long] {
    override def compute(): Long = {
      val n = list.length / 2
      if (n == 0) {
        list match {
          case List() => 0
          case List(n) => n
        }
      } else {
        val (left, right) = list.splitAt(n)
        val leftTask = new AggregateTask(left)
        val rightTask = new AggregateTask(right)
        leftTask.fork()
        rightTask.fork()
        leftTask.join() + rightTask.join()
      }
    }
  }

  run(() => pool.invoke(new AggregateTask(list)))
}