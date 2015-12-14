package jarden.generics

import jarden.{Animal,Mammal,Human,Dog,Whale}

/**
 * @author john.denny@gmail.com
 */
object Variance3 {
	def main(args: Array[String]) {
		val vers2 = new Fun[Mammal, Mammal] {
			def apply(p: Mammal) =
				new Mammal(p.name + "2")
		}
		moreFun(vers2)
		val vers3 = new Fun[Mammal, Mammal] {
			def apply(p: Mammal) =
				new Human(p.name + "3")
		}
		moreFun(vers3)
		val vers4 = new Fun[Animal, Whale] {
			def apply(a: Animal) =
				new Whale(a.name + "4")
		}
		moreFun(vers4)
		
		println("eso es todo amigos")
	}
	def moreFun(fun: Fun[Mammal, Mammal]) {
		println(fun(new Dog("Lassie")))
	}
}

trait Fun[-A, +B] {
	def apply(a: A): B
}

/*
Notes.
initially trait Fun[A, B]; for vers2 everything is a Mammal
vers3 returns Dog (Dog is a Mammal)
moreFun passes Human to moreFun (same reason)
	i.e. naming a dog after a person!
change Fun to +A or -B -> error; change Fun to -A, +B -> okay

what's does it all mean?
Fun[A, B] mean A and B must be Mammal
	in Fun passed to moreFun (invariant)
-A means A must be superclass of Mammal
	in Fun passed to moreFun (contravariant)
+B means B must be subclass of Mammal
	in Fun passed to moreFun (covariant)
 */

