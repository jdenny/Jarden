package collections

object Labs extends App {
	// removingItems
	// usingMaps
	// simpleList
	// usingOptions
	// swapKeys
	filterAndFlatMap
}

object removingItems {
	def removeNth(list: List[Any], n: Int): List[Any] = 
		list.take(n) ++ list.drop(n+1)
	val names = List("John", "Julie", "Angela", "Sarai")
	val res = removeNth(names, 2)
	val res2 = removeNth(res, 0)
	println("res2=" + res2)
	assert(res2 == List("Julie", "Sarai"))
	
	println("end of removingItems")
}

object usingOptions {
	def toInt(s: String): Option[Int] = {
		try {
			Some(s.toInt)
		} catch {
			case _: NumberFormatException => None
		}
	}
	val nums = List("one", "2", "three", "4")
	nums.map(toInt(_)).foreach { x => println(x) }
	val res = nums.map(toInt(_))
	println("res=" + res)
	val res2 = nums.flatMap(toInt(_))
	println("res2=" + res2)

	println("end of usingOptions")
}

object usingMaps {
	var map = new scala.collection.mutable.HashMap[String, Int]
	def process(token: String) = {
		val count = map.getOrElse(token, 0) + 1
		map.put(token, count)
	}
	val in = new java.util.Scanner(new java.io.File("docs/simple.txt"))
	while (in.hasNext) process(in.next)
	for (entry <- map) println(entry._1 + " = " + entry._2)
	
	println("end of usingMaps")
}

object simpleList {
	sealed trait List[+A] {
		override def toString = {
			def _toString(list: List[A], firstElem: Boolean): String = list match {
				case Nil => ")"
				case cons: Cons[A] => {
					val prefix = if (firstElem) "JList(" else ", "
					prefix + cons.head + _toString(cons.tail, false)
				}
			}
			this match {
				case Nil => "List()"
				case cons: Cons[A] => _toString(this, true)
			}
		}
	}
	case object Nil extends List[Nothing]
	case class Cons[+A](head: A, tail: List[A]) extends List[A]
	
	object List {
		def apply[A](items: A*): List[A] = {
			if (items.length == 0) Nil
			else new Cons(items.head, apply(items.tail: _*))
		}
	}

	val list = new Cons[String]("Jack", Nil)
	val list2 = new Cons[String]("Jill", list)
	val list3 = new Cons[String]("Bill", list2)
	val list4 = List(1, 2, 3)
	println("list=" + list)
	println("list2=" + list2)
	println("list3=" + list3)
	println("list4=" + list4)
	
	println("end of simpleList")
}


object swapKeys {
	val capitals = Map("England" -> "London", "Scotland" -> "Edinburgh")
	val invCaps = capitals.map(kv => (kv._2, kv._1))
	println("invCaps=" + invCaps)
	// alternative syntax:
	val invCaps2 = capitals.map(kv => (kv._2 -> kv._1))
	println("invCaps2=" + invCaps2)
}

object filterAndFlatMap {
	class Employee(val name: String, val reports: Employee*) {
		override def toString = name
	}
	val bill = new Employee("Bill")
	val ben = new Employee("Ben")
	val julie = new Employee("Julie", bill, ben)
	val bob = new Employee("Bob")
	val jack = new Employee("Jack")
	val joe = new Employee("Joe", bob, jack)
	val people = List(bill, ben, julie, bob, jack, joe)
	val mgrs = people.filter(e => !e.reports.isEmpty)
	val res = mgrs.map(m => (m.name, m.reports))
	println("res=" + res)
	val res2 = for {mgr <- mgrs
			emp <- mgr.reports
		} yield (mgr, emp)
	println("res2=" + res2)
	for {mgr <- mgrs
		emp <- mgr.reports
	} println(s"($mgr, $emp)")

}