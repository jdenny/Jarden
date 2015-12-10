package functional

object Slides extends App {
//	slide7
//	slide7a
//	slide10
//	slide11
//	slide12
//	slide13
//	slide13a
	slide24
}

object slide7 {
	// simple implementation of negate
	// see slide7b for more sophisticated version
	val times3 = (n: Int) => 3 * n
	val square = (n: Int) => n * n
	val neg = (n: Int) => -n
	
	// 'negate' inverts the value returned by a predicate
	def negate(f: (Int) => Int) =
		neg compose f
	
	val times3Neg = negate(times3)
	val squareNeg = negate(square)
	assert(-9 == times3Neg(3))
	assert(-16 == squareNeg(4))
	println("end of slide 7")
}

object slide7a {
	val negate = (f: (Int, Int) => Int) => (a: Int, b: Int) => -f(a, b)
	val reverse = (f: (Int, Int) => Int) => (a: Int, b: Int) => f(b, a)
	val power = (a: Int, b: Int) => Math.pow(a, b).asInstanceOf[Int]
	assert(8 == power(2, 3))
	val reversePower = reverse(power)
	assert(9 == reversePower(2, 3))
	def power2(a: Int, b: Int) = Math.pow(a, b).asInstanceOf[Int]
	val reversePower2 = reverse(power2)
	assert(9 == reversePower2(2, 3))
	println("end of slide 7a")
}

object slide10 {
	// difference between val and def
	// val evaluated during definition
	val f = util.Random.nextInt
	// def evaluated during each call
	def g = util.Random.nextInt
	println(s"f=$f")
	println(s"f=$f") // produces same number
	println(s"g=$g")
	println(s"g=$g") // produces different numbers
	println("end of slide 10")
}

object slide11 {
	val flip = (f: (Char, Char) => String) => (a: Char, b: Char) => f(b, a)
	val concat = (a: Char, b: Char) => String.valueOf(a) + String.valueOf(b)
	assert("yx" == flip(concat)('x', 'y'))
	val flipConcat = flip(concat)
	println("flipConcat('j', 'o')=" + flipConcat('j', 'o'))
	println("end of slide 11")
}

object slide12 {
	var n = 10
	def f(x: Int) = x + n
	assert(13 == f(3))
	n += 1
	assert(14 == f(3))
	println("end of slide 12")
}

object slide13 {
	def fib() = {
		var t = (1, -1)
		() => {
			t = (t._1 + t._2, t._1)
			t._1
		}
	}
	val fib2 = fib()
	for (i <- 1 to 10) println(i + ": " + fib2())
	println("end of slide 13")
}

object slide13a {
	def fib(start: Int, count: Int) = {
		var a = 0
		var b = 1
		def next {
			val next = a + b
			a = b
			b = next
		}
		for (i <- 1 until start) next
		var res = Seq[Int]()
		for (i <- 1 to count) {
			res = res :+ a
			next
		}
		res
	}
	println("fib(1, 3)=" + fib(1, 3))
	println("fib(3, 5)=" + fib(3, 5))
	println("end of slide 13a")
}

object slide24 {
	import Stream.cons
	def succ(n: Int): Stream[Int] = Stream.cons(n, succ(n+1))
	val posInts = succ(0)
	posInts.take(5) foreach println
	val fibs: Stream[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map { n => n._1 + n._2}
	println("fibonacci series using Stream:")
	fibs take 12 foreach (n => print("  " + n))
	println
	
	println("end of slide 24")
}

