package mallon.concurrency

import scala.util.Random
import scala.concurrent.Future
//import scala.concurrent._
import scala.util.{Success,Failure}
import scala.concurrent.ExecutionContext.Implicits.global


object Lab2Concurrency {
	val random = new Random()

	def main(args: Array[String]): Unit = {
		val currentThread = Thread.currentThread()
		for (i <- 1 to 10) {
			val futureInt = Future {
				getInt(50 + i)
			}
			futureInt onComplete {
				case Success(rint:Int) => {
					println("onComplete; random int=" + rint)
				}
				case Failure(ex) => println("exception getting random int: " + ex)
			}
		}
		// wait for future thread to finish, as it is a daemon thread
		for (i <- 1 to 10) {
			try {
				println("main thread ticking")
				Thread.sleep(1000)
			} catch {
				case ie:InterruptedException => {
					println("currentThread interrupted")
					currentThread.interrupt()
				}
			}
		}
		println("main thread finishing")
	}
	def getInt(scale:Int):Int = {
		val ranNum = random.nextInt(scale)
		println("getInt(" + ranNum + "); " + Thread.currentThread())
		Thread.sleep(random.nextInt(5) * 1000)
		ranNum
	}

}