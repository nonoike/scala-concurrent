import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

object BlockingQueueStudy extends App {
  val capacity = 10
  val blockingQueue = new ArrayBlockingQueue[Runnable](capacity) // capacity 個を超える要素を `put` しようとするとブロック
  val finishedCount = new AtomicInteger(0)
  var threads = Seq[Thread]()

  for (i <- 1 to 4) {
    val t = new Thread(() => {
      try {
        while (true) {
          // `take()` でブロッキングキューから要素を取り出す場合にブロッキングされる
          // → このとき外部に `interrupt` を呼ばれると  `InterruptedException` がスローされる
          val runnable = blockingQueue.take()
          runnable.run()
        }
      } catch {
        case _: InterruptedException =>
      }
    })
    t.start()
    threads = threads :+ t
  }

  for (i <- 1 to 100) {
    blockingQueue.put(() => {
      Thread.sleep(1000)
      println(s"Runnable: ${i} finished.")
      finishedCount.incrementAndGet()
    })
  }

  while (finishedCount.get() != 100) Thread.sleep(1000)
  threads.foreach(_.interrupt())
}
