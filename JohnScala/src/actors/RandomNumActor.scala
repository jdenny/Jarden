package actors

import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global

object RandomNumActor extends App {

	val rnSystem = ActorSystem("RandomNumbers")
	val rand = rnSystem.actorOf(Props[RandomNumActor], "RandomNumGen")
	
	implicit val timeout = Timeout(2 seconds)
	1 to 5 foreach { n =>
		(rand ? GetRandomInt(n)).mapTo[(Int, Int)].onSuccess {
			case (i, j) => println(s"n=$n; result=($i, $j)")
		}
	}

	val rNumFuture = (rand ? GetRandomInt).mapTo[Int]

	rNumFuture onSuccess {
		case i => println(s"=> $i")
	}
	
	// wait a second for last replies
	Thread.sleep(2000)
	println("end of sleep; about to shutdown")

	rnSystem.shutdown
	println("shutdown done")
}

case class GetRandomInt(seqNum: Int)

class RandomNumActor extends Actor with ActorLogging {

	log.info("Creating the Random Number Generator Actor")
	val rGen = new scala.util.Random

	override def receive = {
		case GetRandomInt(n) => sender ! (n, Math.abs(rGen.nextInt) % 100)
	}
}