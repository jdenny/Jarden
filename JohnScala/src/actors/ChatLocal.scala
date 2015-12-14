package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object ChatLocal extends App {
	implicit val chatSystem = ActorSystem("ChatterLocal")
	val localActor = chatSystem.actorOf(Props[ChatterLocal],
			name="LocalActor")
	localActor ! "START"

	println("supply message to send; q to quit")
	@scala.annotation.tailrec
	def getAndSendMessage {
		val input = scala.io.StdIn.readLine("> ")
		if (input != "q") {
			localActor ! input
			getAndSendMessage
		}
	}
	getAndSendMessage
	chatSystem.shutdown
	println("chatSystem shutdown; adios!")

}

class ChatterLocal extends Actor {
	val remote = context.actorFor("akka.tcp://ChatterRemote@127.0.0.1:2552/user/ChatterRemote")
	
	def receive = {
		case "START" => 
			println("ChatterLocal.receive.START")
			remote ! "hello from local actor"
		case msg:String =>
			println("ChatterLocal received: (" + msg + ")")
	}
}
