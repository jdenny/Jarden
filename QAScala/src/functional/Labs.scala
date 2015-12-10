package functional

object Labs extends App {
	// predicates
	// higherOrderFunctions
	nthItem
}

object predicates {
	val isEven /*: (Int) => Boolean*/ = (a: Int) => a%2 == 0
	assert(isEven(0))
	assert(isEven(2))
	assert(!isEven(-3))
	assert(isEven(-4))
	
	// to show alternative styles
	val negate2 = (f:(Int) => Boolean) => (a: Int) => !f(a)
	val negate: (Int => Boolean) => (Int => Boolean) = f => (a) => !f(a)
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
	assert(Vector((-2,4), (-1,1), (0,0), (1,1), (2,4)) == res)

	println("end of higherOrderFunctions() test")
}

object nthItem {
	// simple way:
	def get2(index: Int, list: List[String]) = list(index)
	// using recursion:
	def get(index: Int, list: List[String]) = {
		def _get(list: List[String], count: Int): String = {
			if (index == count) list.head
			else _get(list.tail, count+1)
		}
		_get(list, 0)
	}
	
	val list = List("zero", "uno", "dos", "tres", "cuatro")
	println("get(2, list)=" + get(2, list))
	assert("dos" == get(2, list))

	println("end of nthItem() test")
}