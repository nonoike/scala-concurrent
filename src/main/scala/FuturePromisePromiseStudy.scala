import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future, Promise}
import scala.util.Random

object PromiseStudy extends App {
  val promiseGetInt = Promise[Int]
  val futureByPromise = promiseGetInt.future // PromiseからFutureを作ることが出来る

  // Promiseが解決されたときに実行される処理をFutureを使って書くことが出来る
  val mappedFuture = futureByPromise.map(i => println(s"Success: $i"))

  // 別スレッドで何か重い処理をして、終わったらPromiseに値を渡す
  Future {
    Thread.sleep(300)
    promiseGetInt.success(1)
  }

  Await.ready(mappedFuture, 5000.millisecond)
}

class CallbackSomething {
  val random = new Random()

  def doSomething(onSuccess: Int => Unit, onFailure: Throwable => Unit): Unit = {
    val i = random.nextInt(10)
    if (i < 5) onSuccess(i) else onFailure(new RuntimeException(i.toString)) // 成功・失敗をランダムに発生させる
  }
}

class FutureSomething {
  val callbackSomething = new CallbackSomething

  def doSomething(): Future[Int] = {
    val promise = Promise[Int]
    callbackSomething.doSomething(i => promise.success(i), t => promise.failure(t))
    promise.future
  }
}

object CallbackFuture extends App {
  val futureSomething = new FutureSomething

  val iFuture = futureSomething.doSomething()
  val jFuture = futureSomething.doSomething()

  val iPlusJ = for {
    i <- iFuture
    j <- jFuture
  } yield i + j

  val result = Await.result(iPlusJ, Duration.Inf)
  println(s"result = $result")
}
