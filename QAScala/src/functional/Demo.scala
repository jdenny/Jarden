package functional

object Demo extends App {
	// functional contrasted with imperative style
	val names = List("John", "Sam", "Joe", "Angela")
	
	val jsb = new scala.collection.mutable.ListBuffer[String]
	for (i <- 0 until names.size) {
		val name = names(i)
		if (name.startsWith("J")) jsb += name
	}
	val js = jsb.toList
	println("names beginning with J: " + js)
	
	val js2 = names.filter(name => name.startsWith("J"))
	println("names beginning with J: " + js2)

	// val as simple value:
	val num: Int = 5
	val num2 = 6
	val num3 = {
		println("hello, num3 under construction")
		7
	}
	
	// val as function
	val subtract: (Int, Int) => Int = (a: Int, b: Int) => a - b
	assert (subtract(5, 3) == 2)
	val add = (a: Int, b: Int) => a + b
	assert (add(5, 3) == 8)
	type FInt2 = (Int, Int) => Int
	val multiply: FInt2 = (a, b) => a * b
	assert (multiply(5, 3) == 15)
	
	val square: (Int) => Int = (a: Int) => a * a
	assert(square(5) == 25)
	val square2 = (a: Int) => a * a
	assert(square2(5) == 25)
	val square3: Int => Int = a => a * a
	assert(square3(5) == 25)
	type FInt1 = Int => Int
	val square4: FInt1 = a => a * a
	assert(square4(-5) == 25)
	
	/* Notes
	 * if > 1 argument, need parentheses - cf add with square
	 * somehow need to tell compiler the types of the arguments
	 *  - explicit type (square, square3 & square4)
	 *  - implicit type (square2)
	 * it's the '=>' that defines it as a function
	 */
	
	// def as higher-order function
	def dsum (f: (Int, Int) => Int) = f(5, 3)
	assert(dsum(subtract) == 2)
	def dsum2 (f: FInt2) = f(5, 3)
	assert(dsum2(add) == 8)
	
	// val as higher-order function
	val arith: (FInt2, Int, Int) => Int = (f, a, b) => f(a, b)
	assert (arith(subtract, 6, 4) == 2)
	val arith2 = (f: FInt2, a: Int, b: Int) => f(a, b)
	assert (arith2(add, 6, 4) == 10)
	val arith3 = (f:(Int, Int) => Int, a: Int, b: Int) => f(a, b)
	assert (arith3(multiply, 6, 4) == 24)
	
	println("all done!")
}