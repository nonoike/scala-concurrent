import java.util.concurrent.atomic.AtomicInteger

object OutOfMemoryWithThread extends App {
  val counter = new AtomicInteger()

  while (true) {
    new Thread(() => {
      println(counter.incrementAndGet())
      Thread.sleep(100000)
    }).start()
  }
  // OutOfMemoryError が発生する -> スレッド作成数の上限を決めずにプログラムを実装する方法は非常に危険
}
