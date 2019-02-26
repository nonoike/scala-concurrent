import java.time.LocalDateTime._

import akka.actor.{Actor, ActorSystem, Inbox, Props}
import akka.event.Logging

import scala.concurrent.duration._
import scala.language.postfixOps

class MyActor extends Actor {
  val log = Logging(context.system, this) // Akka Actor で利用できるログシステムの Logging のインスタンスを取得

  // メールボックスにメッセージが届く度に呼び出され、メッセージを処理する
  override def receive: Receive = {
    case "test" => log.info("receive test")
    case _ => log.info("received unknown message")
  }
}

object ActorStudy extends App {
  val system = ActorSystem("actorStudy") // Actor を実行するにはまずは ActorSystem を用意する
  val props = Props[MyActor] // ActorSystem に Actor を作成させる際の設定をまとめたレシピ
  val myActor = system.actorOf(props, "myActor") // 状態の変更を防ぐ(アクターモデルの仕組みを守る)ため通常は Actor の参照を取得して利用する

  // ! メソッドを利用してメッセージを送る
  myActor ! "test"
  myActor.tell("test", Actor.noSender) // `myActor ! "test"` と同等
  myActor ! "hoge"

  Thread.currentThread().join() // メインスレッドの処理が終わるのを待つ = アプリケーションはずっと実行され続ける
}

// ------------------------------

// 管理のしやすさのため専用のメッセージクラスを定義
case object Greet // 挨拶を要求するメッセージ
case class WhoToGreet(who: String) // 誰に対して挨拶をするのかを設定するメッセージ
case class Greeting(message: String) // 実際の挨拶を表すメッセージ

// 挨拶を行うアクター
class Greeter extends Actor {
  var greeting = "" // 内部的にループで逐次処理が行われるためスレッド拘束が行われている状態

  override def receive: Receive = {
    case WhoToGreet(who) => greeting = s"hello, $who"
    case Greet => {
      if (greeting == "hello, Lightbend") Thread.sleep(2000)
      sender ! Greeting(greeting)
    }
  }
}

// 挨拶をコンソールに出力するアクター
class GreetPrinter extends Actor {
  override def receive: Receive = {
    case Greeting(message) => println(s"message = $message")
  }
}

object HelloAkkaScala extends App {
  println(s"start $now")
  val system = ActorSystem("helloAkka")
  val greeter = system.actorOf(Props[Greeter], "greeter")
  val inbox = Inbox.create(system)

  greeter ! WhoToGreet("akka")
  inbox.send(greeter, Greet) // アクターにメッセージを送る: Greeter アクター内では sender() を用いて inbox を参照可能
  private val result = inbox.receive(5 seconds) // 5 秒間メッセージを待つ
  val Greeting(message1) = result // message1 = hello, akka というメッセージ
  println(s"Greeting: '$message1' ($now)")

  greeter ! WhoToGreet("Lightbend")
  inbox.send(greeter, Greet)
  //  val Greeting(message2) = inbox.receive(1 seconds) // 1 秒以内にメッセージが帰ってこないため TimeoutException
  val Greeting(message2) = inbox.receive(5 seconds)
  println(s"Greeting: '$message2' ($now)")

  // Scheduler を利用したメッセージの送信
  val greetPrinter = system.actorOf(Props[GreetPrinter])
  system.scheduler.schedule(0 seconds, 1 second, greeter, Greet)(system.dispatcher, greetPrinter)
}
