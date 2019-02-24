import scala.annotation.tailrec

trait FactorialExecutor {
  val length = 5000
  val list = (for (i <- 1 to length) yield BigInt(i)).toList

  @tailrec
  final protected def factorial(i: BigInt, acc: BigInt): BigInt = if (i == 0) acc else factorial(i - 1, i * acc)

  protected def run(f: () => BigInt) {
    val start = System.currentTimeMillis()
    val factorialSum = f()
    val time = System.currentTimeMillis() - start

    println(s"factorialSum = ${factorialSum}")
    println(s"time = ${time} msec") // 時間計測は JIT コンパイルや GC の影響を受けるため専用のライブラリを使って行うべき
  }
}

object FactorialSumTrial extends App with FactorialExecutor {
  run(() => list.foldLeft(BigInt(0))((acc, n) => acc + factorial(n, 1)))
}

object ForkJoinFactorialSumStudy extends App with FactorialExecutor {

  import java.util.concurrent.{ForkJoinPool, RecursiveTask}

  val pool = new ForkJoinPool() // ForkJoinPool のインスタンスを作成

  // タスクを分割できるまでは分割し、分割できない場合はその値を取得、分割できる場合は分割してそれらを非同期で実行して結合
  class AggregateTask(list: List[BigInt]) extends RecursiveTask[BigInt] {
    override def compute(): BigInt = {
      val n = list.length / 2
      if (n == 0) { // 分割できなければ計算
        list match {
          case Nil => 0
          case _ => factorial(list.head, BigInt(1))
        }
      } else { // 処理分割できる場合
        val (l, r) = list.splitAt(n)
        val leftTask = new AggregateTask(l)
        val rightTask = new AggregateTask(r)
        leftTask.fork()
        rightTask.fork()
        leftTask.join() + rightTask.join()
      }
    }
  }

  run(() => pool.invoke(new AggregateTask(list)))
}
