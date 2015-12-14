package jarden.generics

object Variance7 {
	def main(args: Array[String]) {
		val fruit = new Fruit
		val apple = new Apple
		val bramley = new Bramley
		val banana = new Banana
		val apples = new Bag7[Apple]
		val apples2 = new Bag7[Apple]
		val bramleys = new Bag7[Bramley]
		val fruits = new Bag7[Fruit]
		// val fruit2: Bag7[Fruit] = apples
		// invariant:
		apples.invB(apples2) // error with fruits or bramleys
		apples.inv(apple)
		apples.inv(bramley) // error with fruit
		// covariant:
		apples.covB(apples2)
		apples.covB(bramleys) // error with fruits
		apples.cov(apple) // error with fruit
		apples.cov(bramley)
		// contravariant:
		apples.contrav(apple)
		apples.contrav(fruit)
		apples.contrav(bramley)
		apples.contravB(fruits)
		apples.contravB(apples) // error with bramleys

        println("\nAdios mi amigita")
	}
	def tryToHaveFun {
		val f1 = new Fun7[Apple, Apple] { def apply(a: Apple) = a}
		doFun(f1)
		val f2 = new Fun7a[Fruit, Bramley] { def apply(a: Fruit) = new Bramley }
		// doFun(f2) // error
		doFun2(f2)
		
	}
	def doFun(f:Fun7[Apple, Apple]) {
		println(f)
	}
	def doFun2(f:Fun7a[Apple, Apple]) {
		println(f)
	}
}

class Fruit
class Apple extends Fruit
class Bramley extends Apple
class Banana extends Fruit
class Bag7[T] {
	def inv(a: T) { println(a) }
	def cov[S<:T](a: S) { println(a) }
	def contrav[S>:T](a: S) { println(a) }
	def invB(bag: Bag7[T]) { println(bag) }
	def covB[S<:T](bag: Bag7[S]) { println(bag) }
	def contravB[S>:T](bag: Bag7[S]) { println(bag) }
}

trait Fun7[A, B] {
	def apply(a: A): B
}

trait Fun7a[-A, +B] {
	def apply(a: A): B
}
