import java.util.concurrent.CountDownLatch

object LatchStudy extends App {
  val latch = new CountDownLatch(3)

  for (i <- 1 to 3) {
    new Thread(() => {
      println(s"Finished and countDown! ${i}")
      latch.countDown()
    }).start()
  }

  new Thread(() => {
    latch.await()
    println("All tasks finished.")
  }).start()
}
