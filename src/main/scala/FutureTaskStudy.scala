import java.util.concurrent.FutureTask

object FutureTaskStudy extends App {

  val futureTask = new FutureTask[Int](() => {
    Thread.sleep(1000)
    println("FutureTask finished")
    2525
  })
  new Thread(futureTask).start()

  new Thread(() => {
    val result = futureTask.get()
    println(s"result: ${result}")
  }).start()
}
