object ThreadRisk extends App {
  private var counter = 0

  def next(): Int = synchronized {
    counter = counter + 1
    counter
  }

  for (_ <- 1 to 10) {
    new Thread(() => for (_ <- 1 to 100000) println(next())).start()
  }
}
