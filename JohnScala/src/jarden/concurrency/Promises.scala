package jarden.concurrency

import scala.concurrent.{ExecutionContext,Future,Promise}
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object Promises {
	var promiseGoodie:Promise[String] = null
	
	def main(args: Array[String]) {
		println("start of main")
		getGoodie()
		provideGoodie()
		// make sure background threads have finished:
		for(i <- 1 to 5) {
			println(s"present: $i" )
			Thread.sleep(500)
		}
        println("\nAdios mi amigita")
	}
	def provideGoodie() {
		val goodies = List("flap-jacks", "corn flakes", "good woman", "dark chocolate")
		for (i <- 0 until goodies.length) {
			while (promiseGoodie == null || promiseGoodie.isCompleted) {
				println("provideGoodie waiting; i=" + i)
				Thread.sleep(1000)
			}
			promiseGoodie.success(goodies(i))
		}
	}
//	@scala.annotation.tailrec
	def getGoodie() {
		promiseGoodie = Promise[String]
		println("getGoodie created promise")
		val futureGoodie = promiseGoodie.future
		futureGoodie.onComplete {
			case Success(v) => {
				println("getGoodie() obtained: " + v)
				getGoodie
			}
			case Failure(ex) => println("getGoodie() failed: " + ex)
		}
	}

}