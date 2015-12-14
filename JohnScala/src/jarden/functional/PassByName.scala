package jarden.functional

import java.util.Date

object PassByName {
	def main(args: Array[String]) {
		showTime(new Date())
		showTimeByName(new Date())
		val counter = new Counter()
		showCounter(counter.getAndIncr())
		showCounterByName(counter.getAndIncr())
		
		println("\nAdios mi amigo")
	}
	def showTime(t: Date) {
		println("t=" + t)
		Thread.sleep(1000)
		println("t=" + t)
	}
	def showTimeByName(t: => Date) {
		println("t=" + t)
		Thread.sleep(1000)
		println("t=" + t)
	}
	def showCounter(ct: Int) {
		println("ct=" + ct)
		println("ct=" + ct)
		println("ct=" + ct)
	}
	def showCounterByName(ct: => Int) {
		println("ct=" + ct)
		println("ct=" + ct)
		println("ct=" + ct)
	}
}
class Counter {
	var ct = 0
	def getAndIncr() = {
		ct += 1
		ct
	}
}
