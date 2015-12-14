package jarden.functional

import java.util.ArrayList

/**
 * @author John
 */
class Person(val name: String, val email: String) {
	override def toString() = "Person(" + name + ", " + email + ")"
}

object ImperativeVsDeclarative extends App {
	val names = Seq("Simon", "Chris", "Alice", "Lynne", "Arthur")
	val peopleDec = declarative()
	println("dec people=" + peopleDec)
	val peopleImp = imperative()
	println("imp people=" + peopleImp)

	def imperative() = {
		var i = 0
		val people = new ArrayList[Person]
		while (i < names.size) {
			val name = names(i)
			if (!name.startsWith("A")) {
				people.add(new Person(name, name + "@scala.com"))
			}
			i = i+1
		}
		people
	}
	def declarative() =
		names.filter(!_.startsWith("A"))
		     .map(name => new Person(name, name + "@scala.com"))
}