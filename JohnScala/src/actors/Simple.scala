package actors

// import akka.actor._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging

object Simple extends App {

	val ttSystem = ActorSystem("TickTock")

	val ticker = ttSystem.actorOf(Props[TickActor], "ticker")
	val tocker = ttSystem.actorOf(Props[TockActor], "tocker")
	println("tocker.path=" + tocker.path)

	ticker ! StartTicking(tocker, 4)

	Thread.sleep(7000)
	println("about to shutdown system")
	ttSystem.shutdown
	println("system shutdown done")
}

sealed abstract class Message

case class StartTicking(actor: ActorRef, count: Int) extends Message
case class TickMessage(count: Int) extends Message
case class TockMessage(count: Int) extends Message

class TickActor extends Actor with ActorLogging {
	log.info("Creating Tick Actor")
	override def receive = {
		case StartTicking(actor, count) =>
			log.info("Starting... Tick");
			actor ! TockMessage(count)
		case TickMessage(count) =>
			log.info(s"Tick($count)")
			if (count > 0) {
				Thread.sleep(500)
				sender ! TockMessage(count - 1)
			}
	}
}
class TockActor extends Actor with ActorLogging {
	log.info("Creating Tock Actor")
	override def receive = {
		case TockMessage(count) =>
			log.info(s"Tock($count)")
			if (count > 0) {
				Thread.sleep(500)
				sender ! TickMessage(count)
			}
	}
}