package jarden.generics

import scala.collection.mutable.ListBuffer

/*
 * if A is superclass of B, then what is relationship of Collection[A] to Collection[B]?
 * Coll[A] and Coll[B] not related: invariance
 * Coll[A] superclass of Coll[B]: covariance
 * Coll[B] superclass of Coll[A]: contravariance
 * 
 * why does it matter? if we allow covariance, then can assign a bag of apples to a bag
 * of fruit: val collA:Coll[A] = new Coll[B]; collA.add(new C())
 * so bag of apples now also has a banana. So if we define the collection as covariant,
 * then we mustn't allow subtypes to be added, e.g. make it immutable.
 * 
 * if we assign a destination to a source, e.g. name = value
 * then we can also assign value to a superclass of name; so destination is in contravariant position
 * and we can also assign a subclass value to name, so value is in covariant position
 * 
 * class Fred[T], T is invariant, +T is covariant, -T is contravariant
 * class Fred[T <: Fruit], T is covariant, i.e. Fruit or a subclass
 * class Fred[T >: Fruit], T is contravariant, i.e. Fruit or a superclass
 * 
 *          A <- B <- C
 *            <- D
 */

class A (val name:String = "a") {
	override def toString() = getClass().getSimpleName() + ":" + this.name
}
class B(name:String = "b") extends A(name)

class C(name:String = "c") extends B(name)

class D(name:String = "d") extends A(name)

class Bag[+T] (private val stuff: Seq[T]) {
	def head: T = stuff.head
	def tail = stuff.tail
	// def put(t:T):Unit = {} // can't add, as it's covariant
	def put [S >: T] (s: S) = new Bag[S](stuff :+ s)
}

class Sack[T] () {
	val listBuff:ListBuffer[T] = new ListBuffer[T]
	def add(t:T):Unit = listBuff += t
	def get(index:Int) = listBuff(index)
	def add(list:List[T]):Unit = list.foreach(f => this.add(f))
	def add(sack:Sack[T]):Unit = for (el <- sack.listBuff) this.add(el)
	def printAll():Unit = { for (el <- listBuff) print(el + " "); println}
}

class OldBolsa[T](val items:List[T]) {
	def append(item:T) = new Bolsa[T](items:+item)
}

class Bolsa[+T](val items:List[T]) {
	def append[U>:T](item:U) = new Bolsa[U](items:+item)
}

object Bolsa {
	def apply[T](items:T *):Bolsa[T] = new Bolsa[T](items.toList)
}

object Variance {
	def useBolsa() {
		val bolsaInts:Bolsa[Int] = Bolsa(1, 2, 3)
		val bolsaA:Bolsa[A] = Bolsa(new A(), new B(), new C(), new D())
		val bolsaB:Bolsa[B] = Bolsa(new B(), new C())
		val bolsaC:Bolsa[C] = Bolsa(new C())
		val bolsaD:Bolsa[D] = Bolsa(new D())
/*1*/	val bolsaA2:Bolsa[A] = bolsaB
/*2*/	bolsaB.append(new A())
		
	}
/*
1. uncomment line 1; problem? how to fix?
make covariant; how?
class Bag[+T]
2. uncomment line 2; why problem? how to fix? note: can have bag with As & Bs
make append contravariant; how?
	def append[U>:T](item:U) = new Bag[U](items:+item)
 */

	def main(args: Array[String]): Unit = {
		useBolsa()
		
		val mySackA:Sack[A] = new Sack[A]() 
		mySackA.add(new A("a11"))
		mySackA.add(new B("b11"))
		mySackA.add(new C("c11"))
		val alist21 = List[A](new A("a21"), new B("b21"), new C("c21"))
		mySackA.add(alist21)
		val blist22 = List[B](new B("b22"), new C("c22"))
		mySackA.add(blist22) // allowed because List is covariant
		val mySackB:Sack[B] = new Sack[B]()
		mySackB.add(new B("b31"))
		mySackB.add(new C("c31"))
		// mySackA.add(mySackB) // Sack is invariant
		mySackA.printAll

		println("Scala arrays are invariant")
		val aArray:Array[A] = Array[A](new A("a1"), new B("b1"), new C("c1"))
		val bArray:Array[B] = Array[B](new B("b2"), new C("c2"))
		val cArray:Array[C] = Array[C](new C("c3"))
		// val a2Array:Array[A] = bArray // compiler error
		
		val bag:Bag[A] = new Bag[B](Seq(new B("b41"), new C("c41")))
		println(bag.head)
		println(bag.tail)
		
		println("Scala collections are invariant, covariant or contravariant");
		println("Scala List is covariant - defined as List[+A]") 
		val bList:List[B] = List[B](new B("b3"), new B("b4"))
		val aList:List[A] = bList 
		aList.foreach(println)
		
		println("Scala ListBuffer is invariant - defined as ListBuffer[A]")
		val aListBuffer = ListBuffer[A](new A("a5"), new B("b5"), new C("c5"))
		val bListBuffer = ListBuffer[B](new B("b6"), new C("c6"))
		val cListBuffer = ListBuffer[C](new C("c7"))
		// val aListBuffer:ListBuffer[A] = bList // compiler error

		println("merging listBuffers")
		mergeListBuffers2(aListBuffer, cListBuffer);
		println("a + c-ListBuffer")
		aListBuffer.foreach(println)
	}
//	def mergeLists(listB:List[B], listB2:List[B]) = {
//		List[B](listB.toSeq + listB2.toSeq)
//	}
	
	def mergeListBuffers(listB:ListBuffer[B], listB2:ListBuffer[B]) = {
		listB2.foreach(e => listB += e)
	}
	def mergeListBuffers2[Y>:B, Z<:B](dest:ListBuffer[Y], source:ListBuffer[Z]) = {
		source.foreach(dest += _) // or (x => dest += x)
	}
	/*
	 * We are taking out of listB2; the individual objects in listB2 are B's, or
	 * any subclass of B, e.g. Cs, hence we can just as easily take the Cs from
	 * a collection of Cs.
	 * We are putting the individual objects into a collection of Bs. Any superclass
	 * of B is also a B, so we can just as easily put the objects into a collection
	 * of that superclass.
	 */
	/*
	private static void mergeLists(List<? super B> listB, List<? extends B> listB2) {
		for (B b: listB2) listB.add(b);
	}
	// more generic version of above
	private static <T> void mergeLists2(List<? super T> list, List<? extends T> list2) {
		for (T t: list2) list.add(t);
	}
	* 
	*/
	def f1(numbers:Int*) {
		println("head=" + numbers.head)
		println("tail=" + numbers.tail)
	}
	
}

