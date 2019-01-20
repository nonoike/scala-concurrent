import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

object LongLock extends App {
  for (elem <- 1 to 100) {
    new Thread(() => println(NumAndCurrentDateProvider.next)).start()
  }
}

object NumAndCurrentDateProvider {
  private[this] val lastNumber = new AtomicReference[BigInt](BigInt(0))

  //  def next: (BigInt, LocalDateTime) = synchronized { // 可変の状態へのアクセスはないため並列に行う
  def next: (BigInt, LocalDateTime) = {
    val nextNumber = lastNumber.updateAndGet((t: BigInt) => t + 1)
    (nextNumber, currentDateSoHeavy)
  }

  def currentDateSoHeavy: LocalDateTime = {
    Thread.sleep(100)
    LocalDateTime.now()
  }
}
