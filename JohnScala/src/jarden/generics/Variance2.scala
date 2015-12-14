package temp

/*
TODO: merge Variance3 into this file
	Slides:
[inheritance & polymorphism]
								<- Banana
Any <- AnyRef <- Plant <- Fruit <- Apple
					   <- Vegetable <- Carrot
									<- Onion
val fr:Fruit = new Fruit()
replace right-hand side? - Banana, Apple, i.e. any subclass
replace left-hand side? - val pl:Plant, i.e. any superclass

$generics
Bag[T] - invariant, i.e. no link Bag[Fruit], Bag[Apple]
CoBag[+T] - covariant, i.e. CoBag[Apple] is subtype of CoBag[Fruit]
ContraBag[-T] - contravariant: ContraBag[Fruit] is subtype of ContraBag[Apple]

$invariant - Bag[T]
why isn't Bag[Apples] subtype of Bag[Fruit]?
	val bagApples:Bag[Apples] = new Bag[Apples](...)
	val bagFruit:Bag[Fruit] = bagApples // won't compile
if allowed, then:
	bagFruit.add(banana) // seems ok...
but now we've added banana to bag of apples!

$covariant - CoBag[+T]
now CoBag[Apple] *is* subtype of CoBag[Fruit]
	val bagApples:CoBag[Apple] = new CoBag[Apple](...)
	val bagFruit:CoBag[Fruit] = bagApples // does compile
how come? what's the difference? now we can't add to bag
	class CoBag[+T](val elem:T*) {
		def add(t: T) ... // won't compile

$contravariant - ContraBag[-T]
ContraBag[Apples] is supertype of ContraBag[Fruit]
what's the use?
	trait Func[-A, +B] {
		def apply(a:A):B
	def fAppleToFruit = new Func[Apple, Fruit] {
		override def apply(a:Apple):Fruit = new Banana(a.name)
	}
	def fFruitToApple = new Func[Fruit, Apple] {
		override def apply(fr:Fruit):Apple = new Apple(fr.name)
	}
	def doA2F(f:Func[Apple, Fruit], fr:Apple) =
		println(f(fr))
	def main(_args: Array[String]) {
		doA2F(fFruitToApple, apple)
		doA2F(fAppleToFruit, apple)
	
}

 */
trait Func[-A, +B] /*extends AnyRef*/ {
	def apply(a:A):B
}

object Variance2 {
	val apple = new Apple("bramley apple")
	val banana = new Banana("banana")
	val onion = new Onion("red onion")
	val carrot = new Carrot("carrot")
	def times2(n:Int) = n * 2
	def timesmn(m:Int, n:Int) = m * n
	def fAppleToFruit = new Func[Apple, Fruit] {
		override def apply(a:Apple):Fruit = new Banana(a.name)
	}
	// fFruitToApple is a sub-type of fAppleToFruit!
	def fFruitToApple = new Func[Fruit, Apple] {
		override def apply(fr:Fruit):Apple = new Apple(fr.name)
	}
	// fPlantToBanana is a also sub-type of fAppleToFruit
	def fPlantToBanana = new Func[Plant, Banana] {
		override def apply(pl:Plant):Banana = new Banana(pl.name)
	}
	def doA2F(f:Func[Apple, Fruit], fr:Apple) =
		println(f(fr))
	def main(_args: Array[String]) {
		// testFruit()
		// FruitToApple is subtype of AppleToFruit
		val fA2Fxx:Func[Apple, Fruit] = fFruitToApple
		// but AppleToFruit is not subtype of FruitToApple
		// val fF2Axx:Func[Fruit, Apple] = fAppleToFruit
		
		doA2F(fFruitToApple, apple)
		doA2F(fAppleToFruit, apple)
		doA2F(fPlantToBanana, apple)
		
		println("\nAdios mi amigo")
	}
	def testFruit() {
		var cobagFruit = new CoBag[Fruit](banana, apple)
		var bagApples = new CoBag[Apple](new Apple("apple2"), new Apple("apple3"))
		var bagBananas = new CoBag[Banana](banana, new Banana("banana2"))
		cobagFruit = bagApples // only if T+
		// bagApples = bagFruit // only if T-
		// bagBananas = bagApples // no
		var plant:Plant = banana
		println(cobagFruit.set(plant))
	}
	def variantDemo() {
		var cobagFruit = new CoBag[Fruit](banana, apple)
		var cobagApple = new CoBag[Apple](apple)
		var inbagFruit = new InBag[Fruit]
		var inbagApple = new InBag[Apple]
		var contrabagFruit = new ContraBag[Fruit]
		var contrabagApple = new ContraBag[Apple]
		// inbagFruit = inbagApple // invariant; no link
		// inbagApple = inbagFruit // invariant; no link
		cobagFruit = cobagApple // covariant; subtype
		// cobagApple = cobagFruit // covariant; not supertype
		// contrabagFruit = contrabagApple // contravariant; not subtype
		contrabagApple = contrabagFruit // contravariant; supertype
		
	}
}

abstract class Plant (val name: String) {
	override def toString() = getClass().getName + ": " + name
}

abstract class Fruit(name:String) extends Plant(name)
	class Apple(name:String) extends Fruit(name)
	class Banana(name:String) extends Fruit(name)
abstract class Vegetable(name:String) extends Plant(name)
	class Carrot(name:String) extends Vegetable(name)
	class Onion(name:String) extends Vegetable(name)


// import scala.reflect.ClassTag
class CoBag[+T](val elem:T*) {
	def get() = elem
	def set[S >: T](s: S) = new CoBag[S](s)
	// def add(t:T) = 5 // only allowed for contravariant
}

class InBag[T](val elem:T*)

class ContraBag[-T] {
	def put(t:T) = {}
}
