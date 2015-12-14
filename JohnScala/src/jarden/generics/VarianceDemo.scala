package jarden.generics

/*
Morgan Stanley Presentation 24th Sept 2015
Before sides part 1, put note 1 on white board; set font size on PC
Tuples slide; copy test code from MyTuple2 to Temp; write class live!
Mutable Sequences slide; LinkedList deprecated; could use ListBuffer
slides part 2
if time, show ContravarianceDemo

 */
/**
 * @author john.denny@gmail.com
 */
object VarianceDemo extends App {
	trait Fly
	class Animal { override def toString = this.getClass.getSimpleName }
	class Bird extends Animal with Fly
	class Crow extends Bird
	class Plane extends Fly
	
	// which of these will compile?
//	val bird1: Bird = new Bird
//	val bird2: Bird = new Crow
//	val bird3: Bird = new Animal
//	
//	val animal: Animal = new Bird
//	val bird: Bird = new Bird
//	val crow: Crow = new Bird
	/* Note 1
	val a: A    =			expr
	--------        		----
	A is compile-time type	expr produces runtime type
	put()					get()
	consume					supply
	contravariant			covariant position
		position
	 This is normal subtyping; for given rhs, lhs can be any
	 superclass of rhs;
	 conversely for given lhs, rhs can be any subclass of lhs.
	 
	 More generally, subtyping: b is subtype of a if
	 b can be used where a is specified.
	 
	 Let's try this out in a simple class.
	 */
	// don't use any variance:
	class InvBag[T](val t: T*) {
		def get = t.head
		def put(t2: T) = new InvBag[T](t :+ t2: _*)
		def put2[S>:T](t2: S) = new InvBag[S](t :+ t2: _*)
		override def toString = "InvBag: " + t
	}
	val invBagBird: InvBag[Bird] = new InvBag[Bird](new Bird, new Crow)
	val inv:Bird = invBagBird.get
	val invRes = invBagBird.put(new Crow); println("invRes=" + invRes)
	val anyHello: AnyRef = "hello"
	val invRes2 = invBagBird.put2(anyHello); println("invRes=" + invRes)

	// try covariance:
//	val invBagBird2: InvBag[Bird] = new InvBag[Crow](new Crow)
	class CovBag[+T](val t: T*) {
		def get = t.head // see Note 2 below
//		def put(t2: T) = new CovBag[T](t :+ t2: _*) // see Note 3 below
		def put2[S>:T](t2: S) = new CovBag[S](t :+ t2: _*) // see Note 4
		override def toString = "CovBag: " + t
	}
	val covBagBird: CovBag[Bird] = new CovBag[Crow](new Crow)
	val cov:Bird = covBagBird.get; println("cov=" + cov)
	val covResAnimal = covBagBird.put2(new Animal); println("covResAnimal=" + covResAnimal)
	val covResBird = covBagBird.put2(new Bird); println("covResBird=" + covResBird)
	val covResCrow = covBagBird.put2(new Crow); println("covResCrow=" + covResCrow)
	val convResAny = covBagBird.put2(anyHello); println("convResAny=" + convResAny)
	
	/* Notes 
	Note 2: 'get' can be covariant; code that gets values from
	CovBag[Bird] is expecting Birds and Crows; if referenced object is a
	CovBag[Crow] we will get only Crows, which is still okay.
	
	Note 3: 'put' cannot be covariant; code that puts values into a
	CovBag[Bird] can put Birds and Crows; but we're actually referencing a
	CovBag[Crow] which cannot hold a Bird
//	val covRes = covBagBird.put(new Bird); // covBagBird references CovBag[Crow]
	
	Note 4: because of Note 3, the new CovBag must be the type
	of the object passed to put, which is a superclass of the
	referenced object type. In this case the object is CovBag[Crow],
	the passed object is Bird (a superclass), thus the new CovBag is
	type CovBag[Bird]
	*/

	// try contravariance:
//	val invBagBird2: InvBag[Bird] = new InvBag[Animal](new Animal, new Bird, new Crow)
	class ContravBag[-T] (t: T*) { // See Note 5
//		def get = t.head // see Note 6
		def put(t2: T) = new ContravBag[T](t :+ t2: _*) // see Note 7
//		def put2[S>:T](t2: S) = new ContravBag[S](t :+ t2: _*) // Note 8
		override def toString = "ContravBag: " + t
	}
	val contravBagBird: ContravBag[Bird] = new ContravBag[Animal](new Animal, new Bird, new Crow)
	val contravResBird = contravBagBird.put(new Bird); println("contravResBird=" + contravResBird)
	val contravResCrow = contravBagBird.put(new Crow); println("contravResCrow=" + contravResCrow)
//	val contravResPlane = contravBagBird.put2(new Plane)
	
	/* Notes
	Note 5: no val or var on parameter, which both generate getter
	method - see Note 6
	
	Note 6: 'get' cannot be contravariant; code that gets values from
	ContravBag[Bird] gets Birds and Crows; if we referenced a ContravBag[Animal] it
	could get an Animal, which ContravBag[Bird] can't cope with.
//	val contrav:Bird = contravBagBird.get // contravBagBird refers to ContravBag[Animal]
	
	Note 7: 'put' can be contravariant; we can put Birds and Crows into a
	ContravBag[Bird]; if are referencing a ContravBag[Animal], this is OK.

	Note 8: if allowed, we would be able to put any superclass object of Bird
	to contravBagBird, e.g.
//	val contravResPlane = contravBagBird.put2(new Plane)
	and compiler would think resulting type is ContravBag[Fly], but contravBagBird
	already contains Animal, which can't fly
 
	Summary: can use covariance and contravariance in certain situations;
	in the examples above we have seen:
			covariance: can take things out, but cannot put things in 
			contravariance:  can put things in, but cannot take things out
	The rules are to prevent at compile time what would be a runtime exception.
	
	What is the real-world application of this? If a collection or generic function is
	covariant, then Bag[Apple] is a subtype of Bag[Fruit]; so a bag of apples can
	be used where a bag of fruit is expected; the elements are all fruit;
	but the bag must be immutable, so you cannot add more fruit to the bag. The bag
	is a supplier.
	If the function (or collection) is contravariant, then Function[Animal] is a subtype of
	Function[Bird]. E.g. if a garage can service all cars, it must be able to service a 
	Ford, e.g. service[Car] can be used where service[Ford] is expected; the bag is a
	provider. See ContravarianceDemo.
	
	Examples of contravariance:
									object	function
	Vet, DogVet, LabradorVet:		Dog		groom, inject 
	Garage, VWGarage, GolfGarage: 	Car		service
	FoodShop, FruitShop, AppleShop:	Food	sell
	in all of these cases, we are performing a function on the object, i.e.
	consuming the object, not supplying the object
	
	demo with tissue, tissue box and ladies handbag
		invariant box (put & take): can put and take tissues from tissue box
		contravariant box: substitute super bag; put yes, get no
		invariant handbag: can put and take articles found in ladies handbag
		covariant handbag: substitute smaller bag; put no, get yes
		
		contravariant tissue box	covariant handbag
		substitute:					substitute:
		get:						get:
		put:						put:
		
		
		I want to put my (clean!) tissue somewhere
			can put it in tissue box; or if full, into handbag (contravariance)
			take from box; empty; can I substitute handbag for tissue box? no
		I want to take out an item I would find in a ladies handbag
			can take out lipstick; can I substitute tissue box? yes (covariance)
			can put lipstick into bag? yes. tissue box? no
	 */

	println("adios mi amigita")
}