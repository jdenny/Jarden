package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object ChatRemote extends App {
	val chatSystem = ActorSystem("ChatterRemote")
	val remoteActor = chatSystem.actorOf(Props[ChatterRemote], name = "ChatterRemote")
	println("remoteActor=" + remoteActor)
	println("The RemoteActor is alive")
}

class ChatterRemote extends Actor {
	def receive = {
		case msg:String =>
			println("ChatterRemote.receive(" + msg + ")")
			sender ! "received " + msg
	}
}

