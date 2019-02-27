import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorSystem, DeathPactException, Inbox, OneForOneStrategy, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

case class DivideRandomMessage(numerator: Int) // 子アクターの RandomDivider に割り算を指示するメッセージ

case class AnswerMessage(num: Int) // 答えを親アクターの AnswerMessage が取得するメッセージ

case class ListDivideRandomMessage(numeratorList: Seq[Int]) // 親アクターである ListRandomDivider に指示するメッセージ

class RandomDivider extends Actor {
  val random = new Random()
  val denominator = random.nextInt(3) // ランダムで 0, 1, 2のどれかで割る。 0 で割るアクターは壊れている(要再起動)

  def receive = {
    case m@DivideRandomMessage(numerator) =>
      val answer = Try {
        AnswerMessage(numerator / denominator)
      } match {
        case Success(a) => a
        case Failure(e) =>
          self.forward(m) // メッセージを自分自身に転送 -> 生まれ変わった自分自身に仕事を処理させるために再起動が掛かる前にメッセージを再度投函
          throw e
      }
      println(s" $numerator / $denominator is $answer")
      sender() ! answer // AnswerMessage を返す
  }
}

class ListRandomDivider extends Actor {
  var listDivideMessageSender = Actor.noSender // 送り主のアクターの参照
  var sum = 0 // 答えを合計するための値
  var answerCount = 0 // 子アクターから受け取った答え
  var totalAnswerCount = 0 // 全体の子アクターから受け取るべき答え

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, 10 seconds) {
      case _: ArithmeticException => {
        println("Restart by ArithmeticException")
        Restart
      }
      case _: ActorInitializationException => Stop
      case _: ActorKilledException => Stop
      case _: DeathPactException => Stop
      case _: Exception => Restart
    }

  val router = {
    val routees = Vector.fill(4) { // 1, 2, 3, 4 という 4つの数のリスト
      ActorRefRoutee(context.actorOf(Props[RandomDivider])) // 整数一つ一つの処理は処理を子アクターの RandomDividerにまかせる
    }
    // Router: 複数のアクターを作成した際に、それにどのようにメッセージを送るのかを定義してくれる仕組み
    // ラウンドロビン: 一つ一つ巡回しながらメッセージを送ってくれるようにする仕組み
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case ListDivideRandomMessage(numeratorList) => {
      listDivideMessageSender = sender() // sender() をフィールドに取得
      totalAnswerCount = numeratorList.size // 取得すべき全体の答えの数を設定
      numeratorList.foreach(n => router.route(DivideRandomMessage(n), self)) // 各アクターに送信
    }
    case AnswerMessage(num) => { // リストの数を割る数で割った商の合計を取得し、合計を求める
      sum += num
      answerCount += 1
      if (answerCount == totalAnswerCount) listDivideMessageSender ! sum // すべて回答したら合計の整数を返す
    }
  }
}

// 各アクターを動かす処理
object RandomDivide extends App {
  val system = ActorSystem("randomDivide")
  val inbox = Inbox.create(system)
  implicit val sender = inbox.getRef()

  val listRandomDivider = system.actorOf(Props[ListRandomDivider], "listRandomDivider")
  listRandomDivider ! ListDivideRandomMessage(Seq(1, 2, 3, 4))
  val result = inbox.receive(10 seconds)
  println(s"Result: $result")

  Await.ready(system.terminate(), Duration.Inf)
}
