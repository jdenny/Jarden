package test

import jarden.concurrency.BlockingQueueMap
import java.util.concurrent.{Executors,Executor,ExecutorService}

import scala.concurrent.{Future,ExecutionContext}
import scala.util.{Success, Failure}

/*
Create several threads (Futures) that at random intervals, add different sites
to collection. 

Create several threads (Futures) that at random intervals,
take different sites from collection.

Confirm that correct numbers of sites are produced and consumed.
 */
object DoQueueMap extends App {
	println("start of DoQueueMap")
	val verbose = false
	val siteCollection = new BlockingQueueMap[String, Protein](verbose)
	val random = new java.util.Random()
	
	def addSites(siteCode: String, count: Int) {
		for (i <- 1 to count) {
			siteCollection.put(siteCode, new Protein("protein:" + siteCode + i))
			Thread.sleep(random.nextInt(200))
		}
	}
	// Use default execution context:
	// import scala.concurrent.ExecutionContext.Implicits.global
	// or define own; in the case below, executionContext doesn't use
	// daemon threads, whereas the default one does.
	val executor: ExecutorService = Executors.newFixedThreadPool(1);
	implicit val executionContext = ExecutionContext.fromExecutor(executor)

	def takeSites(siteCode: String, count: Int, lastOne: Boolean = false) {
		for (i <- 0 until count) {
			val takeSitesFuture = Future[Protein] {
				siteCollection.take(siteCode)
			}
			takeSitesFuture.onComplete {
				case Success(protein) =>
					println("takeSitesFuture(" + siteCode +
						", " + i + ") success: " + protein)
				case Failure(e) =>
					println("takeSitesFuture(" + siteCode +
						", " + i + ") failure: " + e)
			}
		}
	}
	val addSitesFuture = Future[Unit] {
		println("addSitesFuture thread isDaemon: " + Thread.currentThread().isDaemon())
		addSites("abc", 4)
		addSites("jkl", 16)
		addSites("def", 1)
		addSites("ghi", 9)
	}
	addSitesFuture.onComplete {
		case Success(_) => println("addSitesFuture successfully completed")
		case Failure(e) => println("addSitesFuture failed: " + e)
	}
	takeSites("abc", 1)
	takeSites("def", 1)
	takeSites("ghi", 4)
	takeSites("jkl", 16)
	takeSites("abc", 3)
	takeSites("ghi", 5)
	for (i <- 1 to 12) {
		println("waiting for threads to finish - " + i)
		Thread.sleep(500)
	}
	siteCollection.printStats
	println("end of main thread: adios")
}

class Protein(val name: String) {
	override def toString() = "Protein(" + name + ")"
}

