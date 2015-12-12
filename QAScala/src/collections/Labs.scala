package collections

object Labs extends App {
	// usingOptions
	// swapKeys
	filterAndFlatMap
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
	println("end of usingOptions")
	val res = nums.map(toInt(_))
	println("res=" + res)
	val res2 = nums.flatMap(toInt(_))
	println("res2=" + res2)
}

object swapKeys {
	val capitals = Map("England" -> "London", "Scotland" -> "Edinburgh")
	val invCaps = capitals.map(kv => (kv._2, kv._1))
	println("invCaps=" + invCaps)
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