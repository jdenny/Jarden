package jarden.functional

/**
 * Illustrating multifarious ways of declaring a Scala function
 */
object Functions extends App {
  	def add(a: Int, b: Int) = a + b
	val add2: Function2[Int, Int, Int] = (a, b) => a + b
	val add3: (Int, Int) => Int = (a, b) => a + b
	val add4 = (a: Int, b: Int) => a + b
	val add5: (Int, Int) => Int = _ + _
	type ISumType = (Int, Int) => Int
	val add6: ISumType = (a, b) => a + b
	val add7: ISumType = _ + _
	assert(add(4, 5) == 9)
	assert(add2(4, 5) == 9)
	assert(add3(4, 5) == 9)
	assert(add4(4, 5) == 9)
	assert(add5(4, 5) == 9)
	assert(add6(4, 5) == 9)
	assert(add7(4, 5) == 9)
	
	// high-order function, where 1st parameter is itself a function
	def doF(f:(Int, Int) => Int, a:Int, b:Int) = f(a, b)
	val doF2: (Function2[Int, Int, Int], Int, Int) => Int = (f, a, b) => f(a, b)
	val doF3: ((Int, Int) => Int, Int, Int) => Int = (f, a, b) => f(a, b)
	val doF4 = (f: ((Int, Int) => Int), a: Int, b: Int) => f(a, b)
	val doF5: ((Int, Int) => Int, Int, Int) => Int = _(_, _) // wowabaweeba!
	type DoFType = ((Int, Int) => Int, Int, Int) => Int
	val doF6: DoFType =  (f, a, b) => f(a, b)
	val doF7: DoFType =  _(_, _) // how ridiculous!
	assert(doF(add, 4, 5) == 9) // Note: any combination of doFx and addx will work
	assert(doF2(add, 4, 5) == 9)
	assert(doF3(add2, 4, 5) == 9)
	assert(doF4(add3, 4, 5) == 9)
	assert(doF5(add4, 4, 5) == 9)
	// next 2 lines pass a function literal, aka lambda, aka anonymous function
	assert(doF6((a, b) => a + b, 4, 5) == 9) 
	assert(doF7(_ + _, 4, 5) == 9)

	println("it all seems to add up")
}
