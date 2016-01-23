package functional

object Labs extends App {
	predicates
	higherOrderFunctions
	nthItem
}

object predicates {
	val isEven /*: (Int) => Boolean*/ = (a: Int) => a%2 == 0
	assert(isEven(0))
	assert(isEven(2))
	assert(!isEven(-3))
	assert(isEven(-4))
	
	// to show alternative styles
	val negate: (Int => Boolean) => (Int => Boolean) = f => (a) => !f(a)
	val negate2 = (f:(Int) => Boolean) => (a: Int) => !f(a)
	def negate3(f: (Int) => Boolean): (Int) => Boolean = {
		(a: Int) => !f(a)
	}
	def negate4(f: (Int) => Boolean): (Int) => Boolean = {
		(a) => !f(a)
	}
	def negate5(f: (Int) => Boolean) = {
		(a: Int) => !f(a)
	}
	
	val isOdd = negate(isEven)
	assert(!isOdd(0))
	assert(!isOdd(2))
	assert(isOdd(-3))
	assert(isOdd(21))
	
	println("end of isEven() test")
}

object higherOrderFunctions {
	def values(f: (Int) => Int, start: Int, end: Int) = {
		for (i <- start to end) yield (i, f(i))
	}
	val res = values(x=> x*x, -2, 2)
	println("res=" + res)
	val expected = Vector((-2,4), (-1,1), (0,0), (1,1), (2,4)) 
	assert(res == expected)

	val values2 = (func: Int => Int, start: Int, end: Int) =>
		for (n <- start to end) yield (n, func(n))
	val res2 = values2(x => x * x, -2, 2)
	println("res2=" + res2)
	assert(res2 == expected)

	println("end of higherOrderFunctions() test")
}

object nthItem {
	// simple way:
	def nthSimple(index: Int, list: List[Any]) = list(index)
	// using recursion:
	@scala.annotation.tailrec
	def nth(n: Int, list: List[Any]): Any = {
		if (n == 0) list.head
		else nth(n-1, list.tail)
	}
	// as val:
	@scala.annotation.tailrec
	val nthV: (Int, List[Any]) => Any = (n, list) => {
		if (n == 0) list.head
		else nthV(n-1, list.tail)
	}
	
	val list = List("zero", "uno", "dos", "tres", "cuatro")
	println("nth(2, list)=" + nth(2, list))
	assert("dos" == nth(2, list))

	val numbers = List(23, 34, 45, -2, 3)
	val res2 = nth(3, numbers)
	println(s"res2=$res2")
	assert(res2 == -2)

	println("end of nthItem() test")
}