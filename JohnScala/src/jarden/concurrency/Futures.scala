package jarden.concurrency

import scala.concurrent.{Future,ExecutionContext,Promise}
import ExecutionContext.Implicits.global
import scala.util.{Try,Success,Failure}

/**
 * This has a simple example of using Futures. But because Futures
 * is using a lot of clever Scala features, we first review those
 * features.
 * @author john.denny@gmail.com
 */
object Futures {
	def main(args: Array[String]) {
		println("start of main")
		tryImplicits() // implicit used to define ExecutionContext
		tryTry() // Try is type of object passed to Future.onComplete
		tryPartialFunction() // Future.onFailure and onSuccess take
			// a PartialFunction.
		tryApply() // method apply() used to pass function to Future
		// tryCallByName() // callback parameter TODO: add this!
		
		// now see this at work in real Future object
		val pf = Future[String] { longJob("future", 5) }
		pf.onComplete {
			case Success(message:String) => println(message)
			case Failure(e) => println(e)
		}
		pf.onSuccess{ case s:String => println("on success! " + s)}
		val pf2 = pf.map(s => s + " with extra bit!")
		pf2.onComplete {
			case Success(message:String) => println(message)
			case Failure(e) => println(e)
		}
		// wait for threads to finish:
		import scala.concurrent.duration._
		scala.concurrent.Await.result(pf2, 3 seconds)
//		for(i <- 1 to 8) {
//			println(s"present: $i" )
//			Thread.sleep(500)
//		}
		tryPromise()

		println("\nAdios mi amigo")
	}
	def longJob(name:String, loops:Int) = {
		for(i <- 1 to loops) {
			println(s"longJob($name, $i) thread: ${Thread.currentThread.getName}" )
			Thread.sleep(500)
		}
		"longJob " + name + " finished"
	}
	def tryTry() {
		def tryDivide(a:Int, b:Int): Try[Int] = {
			if (b == 0) Failure(new ArithmeticException("divide by zero"))
			else Success(a / b)
		}
		assert(tryDivide(10, 3) == Success(3))
		assert(tryDivide(10, 0).isFailure)
		for (a <- 0 to 4) {
			val res1:Try[Int] = tryDivide(10, a)
			res1 match {
				case Success(r) => println(s"Success: 10 / $a = $r")
				case Failure(e) => println(s"Failure: 10 / $a = $e")
			}
		}
	}
	// Note: return value may not be defined for this partial
	// function (by definition), in which case the return value
	// from the Future is discarded
	def tryPartialFunction() {
		def tryIt(pf: PartialFunction[Try[Int], Unit]) = {
			pf.apply(Success(25))
			println("now for the explosion...")
			pf.apply(Failure(new IllegalArgumentException("-ve")))
		}
		tryIt {
			// One way (simplest in fact) of defining a partial
			// function is through a series of case statements:
			case Success(i) => println("i=" + i)
			case Failure(ex) => println("exception: " + ex)
			// so we are defining a PartialFunction, passing it to
			// tryIt; tryIt uses this PartialFunction as a callback
			// to pass us back results, either success or failure
		}
	}
	def tryApply() {
		object DoIt {
			def apply(f: (String) => String) {
				println(f("abcd"))
			}
		}
		DoIt{s:String => s.toUpperCase()}
		object MyFuture {
			def apply[T](body: => T)/*: MyFuture[T]*/ {
				println("MyFuture.apply() - before body")
				body
				println("MyFuture.apply() - after body")
			}
		}
		class MyFuture[T] {
			def onComplete(s:String) { println(s) }
			
		}
		// note that longJob isn't run first; the function call is passed
		// to MyFuture, not the results of the call
		val mf = MyFuture[String] { longJob("myFuture", 3) }
		// mf.onComplete("hello")
	}
	def tryPromise() {
		val promise = Promise[String]
		val future1 = promise.future
		val future2 = promise.future
		future1.onComplete {
			case Success(s) => println("future1.success: " + s)
			case Failure(e) => println("future1.failure: " + e)
		}
		future2.onComplete {
			x => x match { // i.e. long form of above
				case Success(s) => println("future2.success: " + s)
				case Failure(e) => println("future2.failure: " + e)
			}
		}
		promise.complete(Success("fullfilled my promise!"))
		Thread.sleep(100) // give futures time to react
	}
	def tryImplicits() {
		class Person(val name: String)
		implicit val defaultPerson = new Person("John Denny")
		def f(num: Int)(implicit person: Person) = {
			person.name.substring(0, num)
		}
		assert(f(4) == "John")
		assert(f(7)(new Person("Julie Dawn")) == "Julie D")
		println("implicits worked okay")
	}

}