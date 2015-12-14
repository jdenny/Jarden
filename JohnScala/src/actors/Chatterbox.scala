package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object Chatterbox {

	def main(args: Array[String]): Unit = {
		val chatSystem = ActorSystem("Chatterbox")
		val me = chatSystem.actorOf(Props[ChatterBox1])
		val her = chatSystem.actorOf(Props[ChatterBox2])
		println("supply message to send; q to quit")
		@scala.annotation.tailrec
		def getAndSendMessage {
			val input = scala.io.StdIn.readLine("> ")
			if (input != "q") {
				me ! SendIt(her, input)
				getAndSendMessage
			}
		}
		getAndSendMessage
		chatSystem.shutdown
		println("chatSystem shutdown; adios!")
	}

}

class MessageIt
case class SendIt(to: ActorRef, text:String) extends MyMessage
case class ReceiveIt(text:String) extends MyMessage


class ChatterBox1 extends Actor {
	def receive = {
		case SendIt(to, text) => {
			println("ChatterBox.SendIt(" + to.path + ", " + text + ")")
			to ! ReceiveIt(text)
		}
		case ReceiveIt(text) => println("ChatterBox.ReceiveIt(" + text + ")")
	}
}

class ChatterBox2 extends Actor {
	def receive = {
		case SendIt(to, text) => {
			println("ChatterBox.SendIt(" + to.path + ", " + text + ")")
			to ! ReceiveIt(text)
		}
		case ReceiveIt(text) => println("ChatterBox.ReceiveIt(" + text + ")")
	}
}
