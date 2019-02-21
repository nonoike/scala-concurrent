import java.util.concurrent.Semaphore

object SemaphoreStudy extends App {
  val semaphore = new Semaphore(3)

  for (i <- 1 to 100) {
    new Thread(() => {
      try {
        semaphore.acquire()
        Thread.sleep(300)
        println(s"Thread finished. ${i}")
      } finally {
        semaphore.release()
      }
    }).start()
  }
}
