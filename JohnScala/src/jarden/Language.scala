package jarden

/*
Traversable => foreach(), isEmpty(), head(), tail() & many more!
	Iterable => iterator() - hasNext(), next()
		Map => get(key)
		Set => no duplicates
		Seq	=> ordered, indexed - apply(index); optimal for LIFO
			Array => mutable
			LinearSeq
				List
			IndexedSeq
				Vector => tree; default implementation of Seq

Nil = List[Nothing] - the empty list 
 */
object Language {
	def main(args: Array[String]): Unit = {
		val names = List("Dos", "Cuatro", "Seis")
		val numbers = List(2, 4, 6)
		val f = (n:Int) => n * n
		
		// control:
		for (a <- 1 to 3) println(a + " squared = " + a*a)
		// iterate through the Seq:
		for (name <- names) { print(name + " "); println }
		names.foreach(println); // or x => println(x) or println(_) or println
		for (a <- 2 to 4; b <- 2 to 5) println(b + " times " + a + " = " + a * b)
		
		// process elements:
		val res1 = names.map(_.toUpperCase())
		names.map(a => a.toUpperCase()) // or _.toUpperCase() or _.toUpperCase
		res1.foreach(n => print(n + " ")); println()

		val res2 = numbers.map(square(_))
		numbers.map(i => square(i))
		numbers.map(f) // or map(f(_)) or map(x => f(x))
		res2.foreach(n => print(n + " ")); println()
		
		// define anonymous functions:
		val g = (n:Int) => square(n)
		// alternatives for g:
		val g2:(Int)=>Int = (n:Int) => square(n)
		val g3:(Int)=>Int = n => square(n)
		val g4:(Int)=>Int = square(_)
		println("g(6)=" + g(6))
		
		val j = () => println("Hello")
		j()

		val k = (x:Int, y:Int) => x+y
		// alternatives for k
		val k2:(Int, Int)=>Int = (x:Int, y:Int) => x+y
		val k3:(Int, Int)=>Int = (x, y) => x+y
		println("k(4, 5)=", k(4, 5))
		
		// clever things:
		println(classOf[Thread])
		class A
		class B extends A {
			def d = new java.util.Date
		}
		val a:A = new B
		println(a.isInstanceOf[B])
		a.asInstanceOf[B].d
		
		val num = 23
		val b:Option[Int] = if (num >= 0) Some(num) else None
		println(b.get)
		
	}
	def square(n:Int) = n * n
	

}