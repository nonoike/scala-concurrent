import java.util.concurrent.CyclicBarrier

object BarrierStudy extends App {

  val barrier = new CyclicBarrier(4, () => {
    println("Barrier Action!")
  })

  for (i <- 1 to 6) {
    new Thread(() => {
      println(s"Thread started. ${i}")
      Thread.sleep(300)
      barrier.await()
      Thread.sleep(300)
      println(s"Thread finished. ${i}")
    }).start()
  }

  Thread.sleep(5000) // スレッド6つ作成うち4つを使用 → 2つがバリアで残ってしまうため強制終了させておく
  System.exit(0)
}
