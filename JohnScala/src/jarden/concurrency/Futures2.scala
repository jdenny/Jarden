package jarden.concurrency

import scala.concurrent.{Future,ExecutionContext,Promise}
import scala.util.{Try,Success,Failure}

/**
 * Demo of scala.concurrent.Future, with normal version (ImplicitFuture)
 * plus more explicit version.
 */
object John extends App {
	// ImplicitFuture.doIt
	ExplicitFuture.doIt
}

object ImplicitFuture {
	import ExecutionContext.Implicits.global
	import scala.concurrent.duration._
	
	def longJob(name:String, loops:Int) = {
		for(i <- 1 to loops) {
			println(s"longJob($name, $i) thread: ${Thread.currentThread.getName}" )
			Thread.sleep(500)
		}
		"longJob " + name + " finished"
	}
	def doIt {
		// start a job in a separate thread;
		// on call back, test for success or failure
		val future1 = Future[String] { longJob("future", 5) }
		future1.onComplete {
			case Success(message: String) => println(message)
			case Failure(e) => println(e)
		}
		
		println("waiting for future1 to finish")
		scala.concurrent.Await.result(future1, 3 seconds)
		println("\nabout to exit doIt")
	}
	/* Questions: (can write Futures without knowing answers!)
	Where did the thread come from? thread pool? daemon? priority?
	What function is being called on Future?
	Why the curly brackets?
	What are we passing to Future?
	How does the call-back work?
		What gets passed back to onComplete?
		What gets passed back to onSuccess?
	 */
}

/**
 * Demo of scala.concurrent.Future, but more explicit than normal.
 * Future makes use of a number of language features:
 * 	Try, apply(), PartialFunction (pattern matching), implicit, pass by name
 */
object ExplicitFuture {
	import java.util.concurrent.{Executors,Executor,ExecutorService}

	val priceMap = Map( "sun" -> 2.3, "icl" -> 1.2, "oracle" -> 3.4, "bea" -> 4.5 )
	def price(name: String): Double = priceMap(name)
	
	def doIt {
		// define own execution context; in this case we won't use
		// daemon threads, whereas the default one does.
		val executor: ExecutorService = Executors.newFixedThreadPool(1);
		implicit val executionContext = ExecutionContext.fromExecutor(executor)
		
		def processResult(name: String)(tryVal: Try[Double]) = tryVal match {
			case Success(value) => println(s"success: price of $name is $value")
			case Failure(ex) => println("failure: " + ex)
		}
		
		val processResultV2 = new PartialFunction[Try[Double], Unit] {
			def isDefinedAt(tryD: Try[Double]) = true
			def apply(tryD: Try[Double]) = tryD match {
				case Success(value) => println("success: " + value)
				case Failure(ex) => println("failure: " + ex)
			}
		}
		
		val future2 = Future.apply[Double](price("icl"))
		future2.onComplete(processResult("icl")_ )
		
		val future3 = Future.apply[Double](price("ibm"))(executionContext)
		future3.onComplete(processResultV2)
		
		val future4 = Future { price("bea") }
		future4.onComplete {
			case Success(message: Double) => println(s"success: price of bea is $message")
			case Failure(e) => println(e)
		}
	}
	
}