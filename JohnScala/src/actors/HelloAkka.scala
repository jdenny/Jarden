package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

/*
TODO next: put Her into separate file; pass message from Me to main;
separate JVMs! separate languages!
 */
object HelloAkka {
	def main(args: Array[String]): Unit = {
		val chatSystem = ActorSystem("Chat1")
		val me = chatSystem.actorOf(Props[Me])
		val her = chatSystem.actorOf(Props[Her])
		println("supply message to send; q to quit")
		// imperative version of code below:
//		var finished = false
//		while (!finished) {
//			val input = scala.io.StdIn.readLine("> ")
//			if (input == "q") finished = true
//			else me ! Send(her, input)
//		}
		@scala.annotation.tailrec
		def getMessageAndSend {
			val input = scala.io.StdIn.readLine("> ")
			if (input != "q") {
				me ! Send(her, input)
				getMessageAndSend
			}
		}
		getMessageAndSend
		chatSystem.shutdown
		println("chatSystem shutdown; adios!")
	}
}

class Me extends Actor {
	def receive = {
		case Send(her, text) => {
			println("Me.Send(" + her + ", " + text + ")")
			her ! Forward(text)
		}
		case Return(text) => println("Me.Return(" + text + ")")
	}
}

class MyMessage
// user sends message to message service (ActorSystem):
case class Send(to: ActorRef, text:String) extends MyMessage
// message service forwards message to recipient:
case class Forward(text:String) extends MyMessage
// recipient returns response to message service:
case class Return(text:String) extends MyMessage

class Her extends Actor {
	def receive = {
		case Forward(text) =>
			println("Her.Forward(" + text + ")")
			sender() ! Return("hi, got your message: " + text)
	}
}