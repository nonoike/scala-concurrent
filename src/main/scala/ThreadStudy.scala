object ThreadStudy extends App {
  println(Thread.currentThread().getName)

  val thread = new Thread(() => {
    Thread.sleep(1000 /*ms*/)
    println(Thread.currentThread().getName)
  })
  thread.start()

  println("main thread finished.")
}
