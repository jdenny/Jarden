package jarden.generics

import jarden.{Animal,Mammal,Human,Dog,Whale}

/**
 * @author john.denny@gmail.com
 */
object Variance4 {
	def main(args: Array[String]) {
		val animalist = List(new Mammal("mammal"), new Dog("Fido"),
				new Human("Adam"), new Whale("Walter"))
		val fun = new Fun2[Mammal, Mammal] {
			def apply(m: Mammal) = new Dog(m.name)
		}
		// Clearly we can pass a subclass of Mammal to Fun2, and we can
		// assign the result to a superclass:
		val res: Animal = fun(new Whale("whale"))
		val res2: Animal = doIt(fun, new Whale("whale"))
		// but what if we wanted it the other way round?
		val funAW = new Fun2[Animal, Whale] {
			def apply(a: Animal) = new Whale(a.name)
		}
		val res3: Whale = funAW(new Animal("animal"))
		val res4 = doIt(funAW, new Mammal("mammal"))
		println("res4=" + res4)

		val map = mapItAll(fun, animalist)
		println(map)
		
		println("eso es todo amigos")
	}
	def doIt(fun: Fun2[Mammal, Mammal], m: Mammal) = fun(m)
	def printItAll(fun: Fun2[Mammal, Mammal], list: List[Mammal]) {
		for (a <- list) println(fun(a))
	}
	def mapItAll(fun: Fun2[Mammal, Mammal], list: List[Mammal]) =
		list.map(fun(_))
}

trait Fun2[-A, +B] {
	def apply(a: A): B
}

/*
Notes.
A cannot be covariant; B cannot be contravariant
is it because can always pass subclass to apply; can always
assign result to superclass, so nothing to be gained?
 */

