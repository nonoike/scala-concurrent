import java.time.LocalDateTime.now

import scala.concurrent.ExecutionContext.Implicits.global // 実行に必要な呼び出しを行う implicit メソッド
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object FutureStudy extends App {
  val s = "Hello"
  val f: Future[String] = Future {
    println(s"call f $now")
    Thread.sleep(1000)
    s"$s future! $now"
  }
  f.foreach { s: String => println(s) } // Future は定義部分が呼び出された瞬間に自動実行される
  println(s"f.isCompleted = ${f.isCompleted} $now")
  Thread.sleep(5000)
  println(s"f.isCompleted = ${f.isCompleted} $now")

  println("----------")

  val f2: Future[String] = Future {
    println(s"call f2 $now")
    Thread.sleep(1000)
    throw new RuntimeException(s"わざと失敗 $now")
  }
  f2.failed.foreach { e: Throwable => println(e.getMessage) }
  f2.foreach { s: String => println(s) }
  println(s"f2.isCompleted = ${f2.isCompleted} $now")
  Thread.sleep(5000)
  println(s"f2.isCompleted = ${f2.isCompleted} $now")

  println("----------")

  val f3: Future[String] = Future {
    println(s"call f3 $now")
    Thread.sleep(1000)
    println(s"[ThreadName] In Future: ${Thread.currentThread.getName}") // ThreadPool
    s"$s future! $now"
  }
  f3.foreach { s: String =>
    println(s"[ThreadName] In Success: ${Thread.currentThread.getName}") // ThreadPool
    println(s)
  }
  println(s"f3.isCompleted = ${f3.isCompleted} $now")
  Await.ready(f3, 5000 millisecond) // Future が終わるまで最大 5000 ミリ秒を待つ
  println(s"[ThreadName] In App: ${Thread.currentThread.getName}  $now") // MainThread
  println(s"f3.isCompleted = ${f3.isCompleted} $now")
}

object FutureOptionUsage extends App {
  val random = new Random()
  val waitMaxMilliSec = 3000

  val futureMilliSec = Future {
    val waitMilliSec = random.nextInt(waitMaxMilliSec)
    if (waitMilliSec < 2000) throw new RuntimeException(s"waitMilliSec is $waitMilliSec")
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }

  // Future は Option としての性質を持つ
  val futureSec = futureMilliSec.map(i => i.toDouble / 1000)

  futureSec onComplete {
    case Success(waitSec) => println(s"Success! $waitSec sec")
    case Failure(t) => println(s"Failure: ${t.getMessage}")
  }

  Thread.sleep(3000)
}

object CompositeFuture extends App {
  val random = new Random()
  val waitMaxMilliSec = 3000

  def waitRandom(futureName: String): Int = {
    val waitMilliSec = random.nextInt(waitMaxMilliSec);
    if (waitMilliSec < 500) throw new RuntimeException(s"$futureName waitMilliSec is $waitMilliSec")
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }

  val futureFirst = Future {
    waitRandom("first")
  }

  val futureSecond = Future {
    waitRandom("second")
  }

  // Future は複数合成可能
  val compositeFuture = for {
    first <- futureFirst
    second <- futureSecond
  } yield (first, second)

  compositeFuture onComplete {
    case Success((first, second)) => println(s"Success! first:$first second:$second")
    case Failure(t) => println(s"Failure: ${t.getMessage}")
  }
  Thread.sleep(5000)
}
