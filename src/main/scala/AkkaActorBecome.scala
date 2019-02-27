import akka.actor.{Actor, ActorSystem, Inbox, Props}

import scala.concurrent.Await
import scala.concurrent.duration._

// become(methodName) で receive メソッドの代わりに methodName メソッドが呼び出される
// unbecome() メソッドを呼び出すことで、元の状態に戻る
class HotSwapActor extends Actor {

  import context._

  def angry: Receive = {
    case "foo" => sender() ! "I am already angry?"
    case "bar" => become(happy)
    case "baz" => unbecome()
  }

  def happy: Receive = {
    case "bar" => sender() ! "I am already happy :-)"
    case "foo" => become(angry)
    case "baz" => unbecome()
  }

  override def receive: Receive = {
    case "foo" => become(angry)
    case "bar" => become(happy)
    case "baz" => sender() ! "I am already sad :<"
  }
}

object HotSwap extends App {
  val system = ActorSystem("hotswap")
  val inbox = Inbox.create(system)
  implicit val sender = inbox.getRef()

  val hotSwapActor = system.actorOf(Props[HotSwapActor], "hotSwapActor")

  hotSwapActor ! "foo" // HotSwapActor が angry 状態になる
  hotSwapActor ! "foo" // HotSwapActor.receive メソッドではなく HotSwapActor.angry メソッドが呼ばれる
  println("foo: " + inbox.receive(5 seconds))

  hotSwapActor ! "bar" // HotSwapActor が happy 状態になる
  hotSwapActor ! "bar" // HotSwapActor.receive メソッドではなく HotSwapActor.happy メソッドが呼ばれる
  println("bar: " + inbox.receive(5 seconds))

  hotSwapActor ! "baz" // HotSwapActor が元の状態になる
  hotSwapActor ! "baz" // HotSwapActor.receive メソッドが呼ばれる
  println("baz: " + inbox.receive(5 seconds))

  Await.ready(system.terminate(), Duration.Inf)
}
