import java.util.concurrent.atomic.AtomicReference

object ReadModifyWrite extends App {
  for (_ <- 1 to 100) {
    new Thread(() => println(FactorialProvider.next)).start()
  }

  object FactorialProvider {
    private[this] val lastNumber = new AtomicReference[BigInt](BigInt(0))
    private[this] val lastFactorial = new AtomicReference[BigInt](BigInt(1))

    def next: BigInt = synchronized {
      val currentNum = lastNumber.get()
      Thread.sleep(100)
      val nextNum = currentNum + 1
      lastNumber.set(nextNum)

      val currentFact = lastFactorial.get()
      Thread.sleep(100)
      val nextFact = currentFact * nextNum
      lastFactorial.set(nextFact)
      nextFact
    }

  }

}
