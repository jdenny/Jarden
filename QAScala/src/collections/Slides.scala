package collections

object Slides extends App {
	// slide5
	// slide6
	slide10
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
	val res = word.groupBy(_.toChar).map { c => (c._1, c._2.length) }
	println("slide version: " + res)
	val res2 = word.foldLeft(Map[Char, Int]() withDefaultValue 0) {
		(h, c) => h.updated(c, h(c) + 1)
	}
	println("slide version2: " + res2)
}