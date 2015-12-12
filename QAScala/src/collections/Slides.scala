package collections

object Slides extends App {
	// slide5
	// slide6
	// slide10
	// slide11
	// slide17
	slide21
}

object slide5 {
	val capitals = Map("England" -> "London", "Scotland" -> "Edinburgh")
	val res = capitals.map {
		case (key, value) => ("Capital of " + key + " is " + value)
	}
	println("res=" + res)
	val res2 = capitals.mapValues { cap => cap.toUpperCase() }
	println("res2=" + res2)
}

object slide6 {
	val sum1st5Squares = (1 to 5).map(x => x * x).sum
	println("sum1st5Squares=" + sum1st5Squares)
}

object slide10 {
	def frequencyMap(s: String) = {
		import scala.collection.mutable.Map
		val fyMap = Map[Char, Int]()
		for (c <- s) {
			val ct = fyMap.get(c)
			ct match {
				case None => fyMap.put(c, 1)
				case si: Some[Int] => fyMap.put(c, ct.get + 1)
			}
		}
		fyMap
	}
	val word = "kayak"
	println("fyMap" + frequencyMap(word))
	val res = word.groupBy(c=>c).map { c => (c._1, c._2.length) }
	println("slide version: " + res)
	val res2 = word.foldLeft(Map[Char, Int]() withDefaultValue 0) {
		(cmap, ch) => cmap.updated(ch, cmap(ch) + 1)
	}
	println("slide 2nd version: " + res2)
}

object slide11 {
	val nums = 1 to 5 toList
	val add1 = (a: Int) => a + 1
	val times2 = (a: Int) => a * 2
	val add3 = (a: Int) => a + 3
	val add1Times2Add3 = add1 compose times2 compose add3
	assert(17 == add1Times2Add3(5))
	val funcs = List(add1, times2, add3)
	val res = nums map (funcs reduceLeft (_ compose _))
	println("res=" + res)
	assert(List(9, 11, 13, 15, 17) == res)
	println("end of slide11")
}

object slide17 {
	val res = for (i <- 1 to 5) yield i * 2
	println("res=" + res)
	val res2 = (1 to 5).map(_ * 2)
	println("res2=" + res2)
	val res3 = (1 to 5).foreach(x => println(x * 2))
	println("end of slide17")
	
}

object slide21 {
	class Person(val name: String, val female: Boolean)
	val people = List(
			new Person("Bill", false),
			new Person("Joe", false),
			new Person("Phillip", false),
			new Person("Jill", true))
	for (person: Person <- people
			if !person.female
			if person.name.contains("ill"))
		println(person.name)
		
	for { person: Person <- people // note curly brackets
			if !person.female
			name = person.name
			if name.contains("ill")
		} println(name)
			
}
