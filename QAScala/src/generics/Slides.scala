package generics

object Slides extends App {
//	slide7
//	slide19
	slide27
}

object slide7 {
	// traversables don't need to maintain state:
	object myTraversable extends Traversable[String] {
		val days = Seq("lunes", "martes", "miercoles",
				"jueves", "viernes")
		def foreach[U](f: String => U) = {
			days.foreach { x => f(x) }
		}
	}
	myTraversable.foreach { 
		x => println(x.head.toUpper + x.tail)
	}
	
	// iterables need to maintain state, in this case 'count'
	object myIterable extends Iterable[String] {
		val days = Seq("lunes", "martes", "miercoles",
				"jueves", "viernes")
		class Iter extends Iterator[String] {
			var count: Int = 0
			def hasNext = count < days.size
			def next = {
				val res = days(count)
				count += 1
				res
			}
		}
		def iterator = new Iter
	}
	val iterator: Iterator[String] = myIterable.iterator
	while (iterator.hasNext) { 
		val s = iterator.next
		println(s.head.toUpper + s.tail)
	}
	println("end of slide7")
}

object slide19 {
	val me = ("John", 42, 123.45)
	val (name, _, rate) = me
	assert ("John" == name)
	assert (123.45 == rate)
	println("end of slide19")
}

object slide27 {
	// see java equivalent at JavaCourse/src/demo/generics/FruitVariance.java

	// scala:
	// variance relationship specified at definition of List:
	// class List[+A] ... 
	abstract class Fruit { }
	class Apple extends Fruit
	class Banana extends Fruit
	class Bramley extends Apple
	class Coxs extends Apple
	
	def processFruitList(appleList: List[Apple]) {
		val fruitList2 = new Banana :: appleList // no change to appleList
		for (apple: Apple <- appleList) println(apple)
	}
	
	val apples = List(new Apple, new Bramley)
	val bramleys = List[Bramley]()
	val fruits = List[Fruit]()
	processFruitList(apples)
	processFruitList(bramleys)
	// processFruitList(fruits) // as FruitList not sub-type of AppleList
}

object slide29 {
	class Fruit
	class Apple extends Fruit
	class Banana extends Fruit
	
	class Bag[A](val items: Seq[A]) {
		def get: A = items.head
		def put[B >: A](b: B) = new Bag[B](items :+ b) 
	}
	
	val fruitBag = new Bag(Seq(new Apple, new Banana))
	val appleBag = new Bag(Seq(new Apple))
	
	val fruit = fruitBag.get
	val apple = appleBag.get
	val fruitBag2 = appleBag.put(new Banana)
	val appleBag2 = appleBag.put(new Apple)
	
	/*
	the '+' in Bag[+A] means Bag[Apple] is a subtype of Bag[Fruit]
	so all methods on Bag[Fruit] are also valid on Bag[Apple]
	 */
}
