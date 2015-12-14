package jarden.maths

import scala.annotation.tailrec

object MathsFunctions {
	def main(args: Array[String]): Unit = {
		println("maths functions")
		val factMap = Map((0, 1), (1, 1), (2, 2), (3, 6), (4, 24), (5, 120), (6, 720))
		factMap.foreach(x => assert(factorial(x._1) == x._2))
		// val binomMap = Map(()) // TODO: have map of samples, like factMap
		assert(quadratic(1, -3, 2) == "(1, 2)")
		assert(quadratic(1, 1, -6) == "(-3, 2)")
		assert(factorial(5) == 120)
		println("factorial(10)=" + ^(10))
		assert(pascal(0,2) == 1) 
		assert(pascal(1,2) == 2) 
		assert(pascal(1,3) == 3) 
		println("printing row 4 of pascal's triangle")
		for (c <- 0 to 4) print(pascal(c, 4) + " ")
		println("sqrt(10, 3)=" + sqrt(10, 3))
		println
		println("end of maths functions")
	}
	def ^(n: Int) = factorial(n)
	
	def factorial(n: Int): Int = {
		@tailrec
		def factor(n: Int, total: Int = 1): Int = 
			if (n < 2) total else factor(n - 1, n * total)
		factor(n, 1)
	}
	
	def quadratic(a: Int, b: Int, c: Int):String = {
		val b2m4ac = b * b - 4 * a * c;
		if (b2m4ac < 0) {
			throw new IllegalArgumentException("no real solution")
		}
		val sqrt: Int = Math.sqrt(b2m4ac).asInstanceOf[Int]
		if (sqrt * sqrt != b2m4ac) {
			throw new IllegalArgumentException("no integer solution")
		}
		val r1 = (-b - sqrt) / (2 * a)
		val r2 = (-b + sqrt) / (2 * a)
		"(" + r1 + ", " + r2 + ")";
	}
	def pascal(c:Int, r:Int): Int = {
		if (c==0) 1
		else if (c==r) 1
		else pascal(c-1, r-1) + pascal(c, r-1)
	}
	// my version of square root, to show tail recursion
	val approx = 0.001
	@tailrec
	def sqrt(x:Double, guess:Double): Double = {
		println("sqrt(" + x + ", " + guess + ")")
		if (goodEnough(x, guess)) guess
		else sqrt(x, improve(x, guess))
	}
	def goodEnough(x:Double, guess:Double) =
		Math.abs(guess * guess - x) < approx
	def improve(x:Double, guess:Double) =
		(x + guess * guess) / (2 * guess)

}

