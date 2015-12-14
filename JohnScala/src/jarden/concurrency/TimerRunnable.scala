package jarden.concurrency

import java.util.concurrent.Executors
import scala.io.StdIn

object TimerRunnable {
	val run1:Runnable = new TimerRunnable("silk")
	val run2:Runnable = new TimerRunnable("cotton")

	def main(args: Array[String]): Unit = {
		println("Supply delay before we interrupt threads,")
		println("in seconds; default to 3")
		val delayStr = StdIn.readLine()
		val delay =
			if (delayStr.length() > 0) Integer.parseInt(delayStr)
			else 3
		// createThreads(delay)
		useThreadPool(delay)
	}
	def createThreads(delay:Int) {
		val silk:Thread = new Thread(run1)
		silk.start()
		val cotton:Thread = new Thread(run2)
		cotton.start()
		Thread.sleep(delay * 1000)
		silk.interrupt()
		cotton.interrupt()
		println("main thread finished sleeping")
	}
	def useThreadPool(delay:Int) {
		val pool = Executors.newFixedThreadPool(2)
		pool.execute(run1)
		pool.execute(run2)
		Thread.sleep(delay * 1000)
		pool.shutdownNow() // typically calls interrupt on each thread
		println("main thread finished sleeping")
	}

}

class TimerRunnable(val name:String, val delayTenths:Int = 5) extends Runnable {
	override def run():Unit = {
		while (!Thread.interrupted()) {
			try {
				println(name)
				Thread.sleep(delayTenths * 100)
			} catch {
				case ie:InterruptedException => {
					println("thread " + name + " interrupted")
					Thread.currentThread.interrupt
				}
			}
		}
		println("thread " + name + " closing down")
	}
}

