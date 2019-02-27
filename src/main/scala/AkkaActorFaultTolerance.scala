import akka.actor.{Actor, ActorRef, ActorSystem, Inbox, Props, SupervisorStrategy}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

class Supervisor extends Actor {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  import scala.concurrent.duration._

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) { // 1 分間に最大 10 回までの再起動を許容 -> 無限リトライの防止
      case _: ArithmeticException => Resume // 続行命令
      case _: NullPointerException => Restart // 再起動命令
      case _: IllegalArgumentException => Stop // 停止命令
      case _: Exception => Escalate // エスカレーション命令
    }

  override def receive: Receive = {
    // Props を受取り、自身の Actor の context を利用して、子アクターを作成してその参照を送信者に送り返す
    case p: Props => sender() ! context.actorOf(p)
  }
}

class Child extends Actor {
  var state = 0

  override def receive: Receive = {
    case ex: Exception => throw ex // 例外自身をスローする
    case x: Int => state = x // 状態をセット
    case "get" => sender() ! state // 送信者に状態を返す
  }
}

object FaultHandlingStudy extends App {
  // ActorSystem のインスタンスの作成と、 Inbox の作成、 Supervisor アクターの作成
  val system = ActorSystem("faultHandlingStudy")
  val inbox = Inbox.create(system)
  implicit val sender = inbox.getRef() // ! でメッセージを送った際の送り主が自動的に inbox となる

  // Supervisor アクターに子アクターの Child アクターの生成を依頼
  val supervisor = system.actorOf(Props[Supervisor], "supervisor")
  supervisor ! Props[Child]
  val child = inbox.receive(5 second).asInstanceOf[ActorRef]

  child ! 42 // state = 42 をセット
  child ! "get" // 状態を取得
  println(s"receive = ${inbox.receive(5 second)}")

  child ! new ArithmeticException // Resume
  child ! "get"
  println(s"receive(Resume) = ${inbox.receive(5 second)}")

  child ! new NullPointerException // Restart
  child ! "get"
  println(s"receive(Restart) = ${inbox.receive(5 second)}")

  inbox.watch(child) // 他のアクターの状態監視をする(Actor が終了した場合に Terminated というオブジェクトをメッセージとして受け取る)
  child ! new IllegalArgumentException // STOP
  println(s"receive(Stop) = ${inbox.receive(5 second)}")


  supervisor ! Props[Child]
  val child2 = inbox.receive(5 seconds).asInstanceOf[ActorRef]
  inbox.watch(child2)
  child2 ! "get"
  println(s"receive(new Actor) = ${inbox.receive(5 second)}")

  child2 ! new Exception("crash and escalate") // Escalate
  println(s"receive(Escalate) = ${inbox.receive(5 second)}")

  Await.ready(system.terminate(), Duration.Inf)
}

