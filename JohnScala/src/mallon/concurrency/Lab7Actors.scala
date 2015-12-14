package mallon.concurrency

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging

import jarden.maths.DoSums

import scala.util.{Try, Success, Failure}

/*
Actor, respond to messages: Add, Subtract, Multiply, Divide + 2 Int values
reply with Int for + - * or Double for /
use asynchronously and synchronously
 */

object Lab3Actors {

	def main(args: Array[String]): Unit = {
		val ttSystem = ActorSystem("TickTock")
	
		val mathsActor = ttSystem.actorOf(Props[MathsActor])
		val clientActor = ttSystem.actorOf(Props[ClientActor])
	
		mathsActor ! DoSumsMessage("15 + 4", clientActor)
		mathsActor ! DoSumsMessage("15 * 4", clientActor)
		mathsActor ! DoSumsMessage("15 ^ 4", clientActor)
		mathsActor ! DoSumsMessage("15 - 4", clientActor)
		mathsActor ! DoSumsMessage("15 / 4", clientActor)
	
		Thread.sleep(5000)
		println("about to shutdown")
		ttSystem.shutdown
	}
}

abstract class Message

case class DoSumsMessage(val request:String, val client:ActorRef) extends Message
case class SumsResultMessage(val response:Try[String]) extends Message


class MathsActor extends Actor with ActorLogging {
	log.info("Creating Maths Actor")
	override def receive = {
		case DoSumsMessage(request, client) =>
			log.info("DoSums(" + request + ")")
			val res = Try {
				"result of " + request + " = " +
				DoSums.evaluateArithmeticExpression(request)
			}
			client ! SumsResultMessage(res)
	}
}

class ClientActor extends Actor with ActorLogging {
	log.info("Creating Client Actor")
	override def receive = {
		case SumsResultMessage(response) => response match {
			case Success(res) => log.info(res)
			case Failure(ex) => log.info(ex.toString())
		}
	}
	
}