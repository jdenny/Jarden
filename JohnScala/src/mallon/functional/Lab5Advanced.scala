package mallon.functional

import scala.annotation.tailrec

object Lab5Advanced {

	def main(args: Array[String]): Unit = {
		println("starting advanced")
		for (n <- 1 to 10) println("fib(10)=" + timed(fib2, 10))
		val res5 = timed(fib, 5)
		println("res5=" + res5)
		val times = for (n <- Range(5, 20, 2)) yield (n, timed(fib, n))
		for (n <- times) println(n)
		printFunc(fib, 15)
		printFunc(fib2, 15)
		printFunc(fib3, 15)
		println("**************now for the head-to-head*********")
		val times3 = for (n <- Range(20, 40, 2)) yield (n, timed(fib, n), timed(fib2, n), timed(fib3, n))
		for (n <- times3) println(n)
		
		println("ending advanced")
	}
	def timed(f: (Int) => Int, a: Int) = {
		val startTime = System.nanoTime()
		(f(a), System.nanoTime() - startTime)
	}
	def printFunc(f:(Int)=>Int, n: Int) = {
		for (a <- 1 to n) print(f(a) + " ")
		println
	}
	def fib(n: Int): Int = if (n < 3) 1 else fib(n - 1) + fib(n - 2)
	def fib2(n: Int): Int = {
		@tailrec def fib_tail(n: Int, a: Int, b: Int): Int = n match {
			case 0 => a
			case _ => fib_tail(n - 1, b, a + b)
		}
		return fib_tail(n, 0, 1)
	}
	def fib3(n: Int): Int = {
		@tailrec def fib_tail(n: Int, a: Int, b: Int): Int = {
			if (n < 3) a+b else fib_tail(n-1, b, a+b)
		}
		return fib_tail(n, 0, 1)
	}

}