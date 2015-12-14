package jarden.generics

/**
 * @author john.denny@gmail.com
 */
object Variance8 extends App {
	class A
	class B extends A
	class C extends B
	
	// which of these will compile?
//	val b1: B = new B
//	val b2: B = new C
//	val b3: B = new A
//	
//	val a: A = new B
//	val b: B = new B
//	val c: C = new B
	/*
	 This is normal subtyping; lhs can be any superclass of rhs
	 conversely rhs can be any subclass or lhs
	 In more complex types (function parameters, collections)
	 getter position (rhs) is covariant position
	 whereas setter position (lhs) is contravariant position
	 */

	// don't use any variance:
	class InvBag[T](var t: T) {
		def get = t
		def put(t2: T) { t = t2 }
	}
	val invBagB: InvBag[B] = new InvBag[B](new B) // or new C
	val inv:B = invBagB.get
	invBagB.put(new C)
	
	// try covariance:
//	val invBagB2: InvBag[B] = new InvBag[C](new C)
	class CovBag[+T](val t: T) {
		def get = t
//		def put(t2: T) { t = t2 }
	}
	val covBagB: CovBag[B] = new CovBag[C](new C)
	val cov:B = covBagB.get
//	covBagB.put(new C)

	/* Notes
	'get' can be covariant; code that gets values from Baggy[B] is expecting Bs and Cs
	if we used a Baggy[C] will get only Cs, which is still okay
	'put' cannot be covariant; code that puts values into a Baggy[B] can put Bs and Cs
	if we used a Baggy[C] instead, it could, we could put a B, which Baggy[C] can't hold
	Note that the 'var t' in the constructor generates a setter method, so is invalid
	we can fix this by changing it to a val
	*/
	
	// try contravariance:
//	val invBagB2: InvBag[B] = new InvBag[A](new C)
	class ContravBag[-T] /*(val t: T)*/ {
//		def get = t
		def put(t2: T): String = t2.toString() // i.e. do something to t2
	}
	val contravBagB: ContravBag[B] = new ContravBag[A]
	// val contrav:B = contravBagB.get
	contravBagB.put(new C)
	
	/* Notes
	'get' cannot be contravariant; code that gets values from Baggy[B] gets Bs and Cs
	if we used a Baggy[A] it could get an A, which Baggy[B] can't cope with
	'put' can be contravariant; code that puts values int a Baggy[B] can put Bs and Cs
	if we used a Baggy[A], this doesn't mind Bs and Cs
	Note that val or var t in the constructor generates a getter method, so is invalid
	 */
	/*
	What is the real-world application of this? If a collection or generic function is
	covariant, the Function[B] is a subtype of Function[A]; so you can assign a bag
	of apples can be used where a bag of fruit is expected; the element are all fruit;
	but the bag must be immutable, so you cannot add more fruit to the bag. The bag
	is a supplier.
	If the function (or collection) is contravariant, then Function[A] is a subtype of
	Function[B]. E.g. if a garage can service all cars, it must be able to service a 
	Ford, e.g. service[Car] can be used where service[Ford] is expected; the bag is a
	provider.
	 */
}