package jarden.concurrency

import java.util.concurrent.{ConcurrentHashMap,ArrayBlockingQueue,BlockingQueue}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/**
 * A map that is thread safe, using java.util.concurrent.ConcurrentHashMap.
 * The values (generic) are held in a java.util.concurrent.BlockingQueue,
 * which blocks on put() if no more room, until there is room, and blocks
 * on take() until there is a value to take.
 * 
 * Test code is in test.DoQueueMap
 * 
 * @author john.denny@gmail.com
 */
class BlockingQueueMap[K, V](verbose: Boolean = false) {
	val queues = new ConcurrentHashMap[K, BlockingQueue[V]]

	/**
	 * Put new dockingSite -> protein into collection of available sites.
	 */
	def put(key: K, value: V) {
		if (verbose) println("put(" + key + ", " + value + ")")
		val queue = getQForKey(key)
		queue.put(value)
	}
	private def getQForKey(key: K) = {
		val queue = queues.get(key)
		if (queue != null) queue
		else {
			val queue2 = new ArrayBlockingQueue[V](20)
			val queue3 = queues.putIfAbsent(key, queue2)
			if (queue3 != null) {
				println("another thread has meanwhile sneaked in a queue!")
				queue3
			} else queue2
		}
	}
	/**
	 * Find and return first matching protein. If none found, block and
	 * wait for one to become available.
	 */
	def take(key: K): V = {
		if (verbose) println("take(" + key + ")")
		val queue = getQForKey(key)
		val value = queue.take() // should block until available!
		if (verbose) println("taken: " + key + ", " + value)
		value
	}
	def printStats {
		val keys = queues.keySet()
		println("BlockingQueueMap; size=" + keys.size())
		val iter = keys.iterator()
		while (iter.hasNext()) {
			val key = iter.next()
			val blockingQ = queues.get(key)
			println("values for key " + key + ":")
			try {
				while (true) println("  " + blockingQ.remove())
			} catch {
				case e: NoSuchElementException => Unit
			}
		}
	}
}