package mallon.functional

import scala.io.StdIn
import scala.Range
import scala.Vector
import scala.util.{Try,Success,Failure}

/*
In REPL:
val some = List(Some(23), None, Some(12))
some.flatten

Questions:
1. sum of squares of first 100 integers
2. result of calculation, given operator and 2 operands, e.g. calculate('+', 4, 6)
 */

class A(val name: String) {
	// note: getClass fails for doubly nested inner-class
	// see https://issues.scala-lang.org/browse/SI-5425
	override def toString = getClass().getSimpleName() + ": " + name
}
class B(name: String) extends A(name)
sealed trait Expression {
	def evaluate: Int = this match {
		// Plus and Minus show alternative styles:
		case Plus(left, right) => left.evaluate + right.evaluate
		case m: Minus => m.left.evaluate - m.right.evaluate
		case Times(left, right) => left.evaluate * right.evaluate
		case Divide(left, right) => left.evaluate / right.evaluate
		case Square(left) => {val res = left.evaluate; res * res}
		case Literal(n) => n
	}
}
case class Plus(left: Expression, right: Expression) extends Expression
case class Minus(left: Expression, right: Expression) extends Expression
case class Times(left: Expression, right: Expression) extends Expression
case class Divide(left: Expression, right: Expression) extends Expression
case class Square(expr: Expression) extends Expression
case class Literal(n: Int) extends Expression

// 2 different ways of supporting extractors():
//case class Trainer(name: String, mainSubject: String)

class Trainer(val name: String, val mainSubject: String)

object Trainer {
	def apply(name: String, mainSubject: String) =
		new Trainer(name, mainSubject)
	def unapply(person: Trainer): Option[(String, String)] =
		new Some(person.name, person.mainSubject)
}

object functionalPDF {
	// TODO: pure functions, closures, reduce
	val names = List("Simon", "Colin", "Andrew", "Lee", "Arthur")

	def main(args: Array[String]): Unit = {
		// functions()
		// returnFunctions
		// composition()
		// liftMethod()
		// collections()
		// permsAlternative
		// extremeScala()
		// optionals()
		// forComprehension()
		// passByName()
		// curriedFunctions()
		// beyondPdf()
		// CurriedFunctionsControl.dbConnection()
		// curryDemo()
		// functionalExceptions()
		// partialFunctions()
		tryATry()
		// exceptions()
		// beLazy
		// streams
		// patternMatchingCaseClass
		// extractors
		// either
	}
	def either() {
		def sqrtWithEither(a: Int): Either[String, Double] =
			if (a < 0) Left(a + " is -ve")
			else Right(Math.sqrt(a))
		
		val nums = Seq(1, -1, 4, -4, 9)
		val res: Seq[Either[String, Double]] = nums.map(a => sqrtWithEither(a))
		// use map to produce Seq of String
		val res2: Seq[String] = res.map(e => e match {
			case Right(r) => "sqrt=" + r
			case Left(l) => l
		})
		println("res2=" + res2)
		// no implicit function defined for flatten, so have to provide my own
		val res3: Seq[Double] = res.flatten(e => e match {
			case Right(r) => Some(r)
			case Left(l) => None
		})
		println("res3=" + res3)
	}
	def extractors() {
		val john = Trainer("John", "Scala")
		val pete = Trainer("Pete", "JavaScript")
		val braun = Trainer("Braun", "Python")
		doIt(john)
		doIt("hello")
		doIt(pete)
		doIt(34)
		doIt(braun)
		
		def doIt(any: Any) = any match {
			case Trainer(name, "Scala") =>
				println(s"hi $name. Are you free to teach Scala today?")
			case Trainer(name, "JavaScript") =>
				println(s"hi $name. Are you free to teach JavaScript next week?")
			case Trainer(name, _) =>
				println(s"hi $name. Can you do some marking today?")
			case _ => println("Not a trainer: " + any)
		}
	}
	def patternMatchingCaseClass() {
		abstract class Root
		case class A(name: String) extends Root
		case class B(name: String) extends Root
		def f(_a1: Root, _a2: Root) = (_a1, _a2) match {
			case (A(n1), A(n2)) => println(s"A($n1), A($n2)")
			case (A(n1), B(n2)) => println(s"A($n1), B($n2)")
			case (B(n1), A(n2)) => println(s"B($n1), A($n2)")
			case (B(n1), B(n2)) => println(s"B($n1), B($n2)")
		}
		val a1 = new A("a1")
		val a2 = new A("a2")
		val b1 = new B("b1")
		val b2 = new B("b2")
		f(a1, a2)
		f(a2, new A("a3"))
		f(a1, b1)
		f(b1, a1)
		f(b1, b2)
		
		// now for some DSL for simple arithmetic expressions:
		
		val expr = Minus(Literal(7), Plus(Literal(3), Literal(2))) // 7 - (3 + 2)
		assert(expr.evaluate == 2)
		println("expression: 7 - (3 + a)")
		for (a <- 1 to 5)
			println("  a=" + a + "; expr=" +
				Minus(Literal(7), Plus(Literal(3), Literal(a))).evaluate)
		println("expression: (4+2)^2 / 2 - (4 * 2)")
		val expr2 = Minus(Divide(Square(Plus(Literal(4), Literal(2))),
				Literal(2)), Times(Literal(4), Literal(2)))
		val expr2Res = expr2.evaluate
		println(expr2Res)
		assert(expr2Res == 10)
		
	}
	def returnFunctions() {
		println("starting returnFunctions()")
		type BinFun = (Int, Int) => Int
		def getArithFunction(operator: Char): BinFun = operator match {
			case '+' => (a: Int, b: Int) => a + b
			case '-' => (a, b) => a - b
			case '*' => _ * _
			case '/' => _ / _
		}
		val getArithFunctionV: (Char) => (Int, Int) => Int = operator =>
			operator match {
			case '+' => (a: Int, b: Int) => a + b
			case '-' => (a: Int, b: Int) => a - b
			case '*' => (a: Int, b: Int) => a * b
			case '/' =>  _ / _ // (a: Int, b: Int) => a / b
		}
		println("15 + 3 = " + getArithFunction('+')(15, 3))
		println("15 - 3 = " + getArithFunction('-')(15, 3))
		println("15 * 3 = " + getArithFunction('*')(15, 3))
		println("15 / 3 = " + getArithFunction('/')(15, 3))
		println("ending returnFunctions()")
	}
	def streams() { // from "What is FP?"
		val listB = List(new B("john leslie denny"))
		val res3 = listB :+ new A("julie dawn")
		val res4 = res3 :+ new A("joseph")
		val res2 = res4 :+ new A("sammy")
	
		val avg = (l:List[A]) => l.foldLeft(0)((tot, el) => tot + el.name.length) / l.size
		def avgD (l:List[A]) = l.foldLeft(0)((tot, el) => tot + el.name.length) / l.size
		println("res2=" + res2)
		println(avg(res2))
		val result = res2.filter(p => p.name.length > avg(res2))
						.sortBy(p => p.name.length)
						.map(p => p.name)
		println("result =" +  result)
	}
	def exceptions() {
		println("starting exceptions()")
		val listOk = List("0", "1", "2", "4")
		assert(listOk.map(s=>s.toInt) ==
			List(0, 1, 2, 4))
		val list = List("0", "1", "2", "three", "4")
		try {
			println(list.map(s=>s.toInt))
		} catch {
			case e: NumberFormatException => println(e + " (as expected)")
		}
		assert(list.map(s=>
			try { Some(s.toInt) } catch { case e: NumberFormatException => None}) ==
				List(Some(0), Some(1), Some(2), None, Some(4))
		)
		assert(list.flatMap(s=>
			try { Some(s.toInt) } catch { case e: NumberFormatException => None}) ==
				List(0, 1, 2, 4)
		)
		// combine above 2 steps into 1
		assert(list.flatMap(e => 
			try { Some(12 / e.toInt) } catch { case e: Exception => None 
			}) == List(12, 6, 3))
		
		import scala.util.control.Exception._
		
		assert(catching (classOf[NumberFormatException]).opt("123".toInt) ==
			Some(123))
		assert(catching (classOf[NumberFormatException]).opt("three".toInt) ==
			None)
		assert((for (a <- list;
					b <- catching (classOf[NumberFormatException]).opt(a.toInt);
					c <- catching (classOf[ArithmeticException]).opt(100 / b))
						yield ( (a,c) )) == List(("1",100), ("2",50), ("4",25)) )
		
//		
//		for (a <- listOk) yield(100 / a.toInt)
//		for (a <- listOk; b <- a.toUpperCase()) yield (a, b)
//		for (a <- listOk;
//			b <- Seq(a.toInt);
//			c <- Seq(100 / b)) yield ( a,c )
		
//		assert((for (a <- list;
//					b <- Seq(Try(a.toInt));
//					c <- Seq(Try(100 / b))) yield ( (a,c) )) == List(2) )
		
		println("ending exceptions()")
	}
	def tryATry() {
		val number:Int = getUserNumber("give me a number")
		println("the number you typed was: " + number)
		println(number + " squared is " + number * number)
	}
	def partialFunctions() {
		println("starting partialFunctions()")
		val squares = Map( 1 -> 1, 2 -> 4, 3 -> 9 )
		val moreSquares = squares.orElse(new MyPartial)
		assert(squares(2) == 4)
		assert(squares.get(5) == None)
		assert(squares.getOrElse(5, 50) == 50) // alternative; default to 50
		assert(moreSquares(2) == 4)
		assert(moreSquares(5) == 25)
		println("ending partialFunctions()")
	}
	class MyPartial extends PartialFunction[Int, Int] {
		override def isDefinedAt(a:Int) = true
		override def apply(a:Int) = a*a
	}
	def functionalExceptions() {
		println("starting functionalExceptions()")
		def divide(a:Int, b:Int) = a / b
		val curriedDivide = (divide _).curried
		val divideInto100 = curriedDivide(100)
		val results = for (b <- 3 to 0 by -1) yield {
			try {
				Some(divideInto100(b))
			} catch {
				case ae:ArithmeticException => None
			}
		}
		assert(results == Vector(Some(33), Some(50), Some(100), None))
		assert(results.flatten == Vector(33, 50, 100))
		println("ending functionalExceptions()")
	}
	def getUserNumber(prompt:String):Int = {
		val line = StdIn.readLine(prompt)
		val userInt = Try (line.toInt)
		userInt match {
			case Success(v) => userInt.get
			case Failure(e) =>
				println("supplied value not an integer!")
				getUserNumber(prompt)
		}
	}
	def curryDemo() {
		println("starting curryDemo()")
		val mult = (a:Int, b:Int) => a * b
		assert(mult(5, 4) == 20)
		val curryMult = mult.curried
		println(curryMult)
		val mult5 = curryMult(5)
		assert(mult5(3) == 15)
		// alternative:
		val mult5a = mult(5, _:Int)
		assert(mult5a(3) == 15)
		println("ending curryDemo()")
	}
	// partially applied and curried functions
	def curriedFunctions() {
		println("starting curriedFunctions()")
		def curriedMult(a:Int)(b:Int) = a * b
		val mult7 = curriedMult(7)_
		assert(mult7(3) == 21)

		// now for some partial functions:
		def linear(a:Int, b:Int, c:Int) = a * b + c
		val lineA3 = linear(3, _:Int, _:Int)
		val lineA3B2C1 = lineA3(2, 1)
		assert (lineA3B2C1 == 7)
		val lineA3B2 = lineA3(2, _:Int)
		assert(lineA3B2(1) == 7)
		val g:Double = 6.674e-11 // Newton metres**2 kilograms**-2
		val earthMass:Double = 5.972e24 // kilograms
		val earthRadius:Double = 6.371e6 // metres
		def getGravity(m1:Double, m2:Double, d:Double) =
			g * m1 * m2 / (d * d)
		val forceToEarth = getGravity(earthMass, _:Double, _:Double)
		val forceManToEarth = forceToEarth(70.0, _:Double)
		val forceManOnEarth = forceManToEarth(earthRadius)
		val forceManInSpace = forceManToEarth(earthRadius * 2)
		val forceManOnEarth2 = getGravity(earthMass, 70, earthRadius)
		println("forceManOnEarth=" + forceManOnEarth)
		println("forceManOnEarth2=" + forceManOnEarth2)
		println("forceManInSpace=" + forceManInSpace)
		println("ending curriedFunctions()")
	}
	def beLazy() {
		lazy val b = {
			println("getting b")
			2
		}
		println("sum(3, b)=" + sum(3, b))
		println("sum(3, b*b)=" + sum(3, b*b))
	}
	def sum(a: Int, b: => Int): Int = {
		println("a=" + a)
		a + b
	}
	def passByName() {
		println("starting passByName()")
		def f(nanoTime: => Long) {
			val d1 = nanoTime
			assert(d1 != nanoTime)
		}
		f(System.nanoTime())

		val nums = List(1, 2, 4, 8, 16)
		val iter = nums.iterator
		// try it with and without '=>':
		def showIt(num: => Int) {
			for (i <- 1 to 4) println(s"i=$i; num=$num")
		}
		showIt(iter.next())
		println("ending passByName()")
	}
	def forComprehension() {
		println("starting forComprehension()")
		val numbers = Range(2, 6)
		// following equivalent to: numbers.map(x => ...)
		assert((for(n <- numbers) yield n * (n+1) / 2) == Vector(3, 6, 10, 15))
		// following equivalent to: numbers.filter(x => x%2 != 0).map(y => y * y)
		assert((for (n <- numbers if (n % 2 != 0)) yield n * n) == Vector(9, 25))
		assert((for (n <- names if (n.charAt(0) != 'A'); i <- 1 to 3)
			yield (n.substring(0, i))) == List("S", "Si", "Sim", "C", "Co", "Col", "L", "Le", "Lee"))
		println("ending forComprehension()")
	}
	def optionals() {
		println("starting optionals()")
		val noA:Option[Int] = None
		val someA:Option[Int] = Some(25)
		assert(someA != None)
		assert(noA == None)
		assert(someA.get == 25)
		assert(someA.getOrElse(1) == 25)
		assert(noA.getOrElse(1) == 1)
		val gbpRates = Map("Rupee" -> 99, "Yen" -> 175)
		assert(gbpRates.get("Rupee") == Some(99))
		assert(gbpRates.get("Euro") == None)
		val getRate = (curr:String) => gbpRates.get(curr) match {
			case Some(s) => s.toString
			case None => "unknown"
		}
		assert(getRate("Yen") == "175")
		assert(getRate("USD") == "unknown")

		val input = List("1", "two", "3")
		val optionToInt = (s:String) => {
			try {
				Some(s.toInt)
			} catch {
				case nfe:NumberFormatException => None
			}
		}
		assert( input.map(a => optionToInt(a)) == List(Some(1), None, Some(3)) )
		assert( input.map(a => optionToInt(a)).flatten == List(1, 3) )
		assert( input.flatMap(a => optionToInt(a)) == List(1, 3) )

		println("ending optionals()")
	}
	/*
	 * exists, forall, map, foldLeft, filter, partition, zip,
	 * flatten, flatmap
	 */
	def collections() {
		println("starting collections()")
		val numbers = Range(2, 7)
		val names = List("Simon", "Colin", "Andrew", "Lee", "Arthur")
		assert(numbers.exists(_ > 4) ==
			true)
		assert(numbers.exists(_ == 7) ==
			false) // stops before 7
		assert(numbers.forall(_ > 4) ==
			false)
		assert(numbers.forall(_ < 10) ==
			true)
		assert(numbers.map(x => 2 * x + 1) ==
			Vector(5, 7, 9, 11, 13))
		val squares = Range(1, 5).map(x => x*x)
		assert(squares.foldLeft(0)((a, b) => a + b) ==
			30) // sum of squares of 1 to 4
		assert(names.foldLeft("")((a, b) => a + b.charAt(0)) ==
			"SCALA")
		assert(numbers.filter(x => x%2 == 0) ==
			Vector(2, 4, 6))
		val numbers2 = Vector(1, 2, 4, 5, 9, 15, 16)
		val square = (x:Int) => x * x
		assert(numbers2.partition(x => square(Math.sqrt(x).asInstanceOf[Int]) == x)
			== (Vector(1, 4, 9, 16), Vector(2, 5, 15)))
		val numbers3 = List(1, 2, 3)
		assert(numbers.zip(numbers3) ==
			Vector((2, 1), (3, 2), (4, 3)))
		val nameSeqList = Seq(names.slice(1,3), List("Ann", "Mary", "Jane"))
		assert(nameSeqList.flatten ==
			Seq("Colin", "Andrew", "Ann", "Mary", "Jane"))
		assert(numbers3.flatMap(x => Vector(x, 2*x, x*x)) ==
			Vector(1, 2, 1, 2, 4, 4, 3, 6, 9))
		val perms = (cs:Seq[Char]) =>
			cs.flatMap(c1 => cs.filter(c2 => c1 != c2).map(c2 => (c1, c2)))
		assert(perms('a' to 'c') ==
			Vector(('a', 'b'), ('a', 'c'), ('b', 'a'),
				('b', 'c'), ('c', 'a'), ('c', 'b')))
		val map = Map(1->"john", 2->"julie", 3->"angela")
		val mapFold = map.fold((0, "family:"))((b, e) => (b._1 + e._1, b._2 + " " + e._2))
		assert(mapFold == (6,"family: john julie angela")) // not guaranteed!
		val mapFoldRight = map.foldRight((0, "family:"))((b, e) => (b._1 + e._1, b._2 + " " + e._2))
		assert(mapFoldRight == (6,"john julie angela family:"))
		val mapFoldLeft = map.foldLeft((0, "family:"))((b, e) => (b._1 + e._1, b._2 + " " + e._2))
		assert(mapFoldLeft == (6,"family: john julie angela"))
		val capitals = Map("England" -> "London", "India" -> "New Delhi", "USA" -> "Washington")
		val countries = Seq("England", "India", "China")
		assert(countries.map(key => capitals.get(key)) ==
			List(Some("London"), Some("New Delhi"), None))
		assert(countries.map(key => capitals.get(key)).flatten ==
			List("London", "New Delhi"))
		assert(countries.flatMap(key => capitals.get(key)) ==
			List("London", "New Delhi"))
		println("ending collections()")
	}
	// variations on the slide to find all permutations of
	// 2 lower-case letters; note, 2nd one already in slides!
	def permsAlternative() {
		def perms(chars: Seq[Char]) = 
			chars.flatMap( a => chars.filter(b => b != a).map(b => (a, b)))
	
		def perms2(chars: Seq[Char]) =
			for (a <- chars; b <- chars if (a != b)) yield(a, b)
		println(perms('a' to 'd'))
		println(perms2('a' to 'd'))
	}
	/*
	 * foreach (nested), map, zip
	 */
	def extremeScala() {
		// if you can keep your head when all about you seems
		// like hieroglyphics...
		val numbers = Range(2, 5)
		(3 to 5).foreach(x =>
			List('+', '-', '*').foreach(c =>
				numbers.map(y =>
					println("c=" + c + "; x=" + x + "; y=" + y +
						"; res=" + getSumOp(c)(x, y)))))
		val numbers3 = Range(3, 11, 2)
		numbers.zip(numbers3).foreach(x =>
			println(x._1 + " * " + x._2 + " = " + getSumOp('*')(x._1, x._2)))
		
	}
	def liftMethod() {
		println("starting liftMethod()")
		class Person(var name:String) {
			override def toString() = this.name
		}
		val ann = new Person("Ann")
		println(ann.toString()) // same as println(ann)
		val f: () => String = ann.toString
		assert(f() == ann.toString())
		ann.name = "Anne-Marie"
		assert(f() == "Anne-Marie")
		println("ending liftMethod()")
	}
	def composition() {
		println("starting composition()")
		val nnp1 = (n:Int) => n * (n+1)
		val d2 = (n:Int) => n / 2
		val j = d2 compose nnp1
		assert(j(5) == 15)
		assert(j(6) == (j(5) + 6))
		println("ending composition()")
	}
	// lambdas, higher-oder functions, partial function
	def functions() {
		println("starting functions()")
		// standard function
		assert(add(3, 5) == 8)
		// first class function: can assign function to a variable
		val f:(Int, Int)=>Int = add
		assert(f(23, 5) == 28)
		// higher-order function, where a parameter is itself a function
		assert(doSum(add, 4, 21) == 25)
		assert(doSum(subtract, 4, 21) == -17)
		assert(doSum(getSumOp('*'), 7, 6) == 42)
		// anonymous functions
		val mult = (a:Int, b:Int) => a * b
		val div = (a:Int, b:Int) => a / b
		val sub = (a:Int, b:Int) => a - b
		// anonymous, higher order function
		val sum = (f:(Int, Int)=>Int, x:Int, y:Int) => f(x, y)
		assert(sum(mult, 3, 7) == 21)
		assert(sum(div, 43, 7) == 6)
		assert(doSum(mult, 4, 5) == 20)
		assert(sum(add, 56, 3) == 59)
		// anonymous function that returns a function, wrapped in Option
		val getSum2: (Char) => Option[(Int, Int) => Int] = c =>
			if (c == '*') Option(mult)
			else if (c == '-') Option(subtract)
			else if (c == '/') Option(div)
			else if (c == '+') Option(add)
			else None
		getSum2('/') match {
			case Some(sum2) => assert(sum(sum2, 24, 8) == 3)
			case _ => throw new IllegalStateException("this shouldn't happen!")
		}
		// alternative to above:
		val getSum3: (Char) => (Int, Int) => Int = c =>
			c match {
				case '*' => mult
				case '-' => sub
				case '/' => div
				case _ => add
			}
		assert(sum(getSum3('*'), 13, 4) == 52)
		assert(sum(getSum3('+'), 13, 4) == 17)
		// partial function:
		object Invert extends PartialFunction[Int, Double] {
			def apply(i:Int) =
				if (i==0)
					throw new IllegalArgumentException("cannot invert zero")
				else (1.0 / i)
			def isDefinedAt(i:Int) = (i != 0)
		}
		assert(Invert.isDefinedAt(5) == true)
		assert(Invert.isDefinedAt(0) == false)
		assert(Invert(4) == 0.25)
		println("ending functions()")
	}
	def add(a:Int, b:Int) = a+b
	def subtract(a:Int, b:Int) = a-b
	def multiply(a:Int, b:Int) = a*b
	def doSum(f:(Int, Int)=>Int, x:Int, y:Int) = f(x, y)
	def getSumOp(c:Char): (Int, Int) => Int = 
		if (c == '*') multiply
		else if (c == '-') subtract
		else add
	// alias for function prototype
	// currying an existing function
	def beyondPdf() {
		println("starting beyondPdf()")
		type Sum = (Int, Int) => Int
		val add:Sum = _ + _
		assert(add(3, 6) == 9)
		val sub:Sum = (a, b) => a - b
		assert(sub(3, 6) == -3)
		val div:Function2[Int, Int, Int] = _ / _
		assert(div(42, 7) == 6)
		def mult(a:Int, b:Int) = a * b
		assert(mult(3, 5) == 15)
		val curriedMult = (mult _).curried
		assert(curriedMult(7)(6) == 42)
		val mult9 = curriedMult(9)
		assert(mult9(5) == 45)
		println("ending beyondPdf()")
	}
	def useType() {
		// anonymous functions
		type Arith = (Int, Int) => Int
		val mult:Arith = (a, b) => a * b
		val add:Arith = _ + _
		val div:Arith = (a, b) => a / b
		val sub = (a:Int, b:Int) => a - b
		// anonymous, higher order function
		val sum = (f:Arith, x:Int, y:Int) => f(x, y)
		def doSumOld(f:Arith, x:Int, y:Int) = f(x, y)
		val doSum = (f:Arith, x:Int, y:Int) => f(x, y)
		assert(sum(mult, 3, 7) == 21)
		assert(sum(div, 43, 7) == 6)
		assert(doSum(mult, 4, 5) == 20)
		assert(sum(add, 56, 3) == 59)
		println("finished Temp")
	}
	
	def answers() {
		println("sum of squares of first 100 integers")
		val max = 101
		println(Range(1,max).map(x=> x*x).sum)
	}
}

object CurriedFunctionsControl {
	// "Working with a database connection"
	def dbConnection() = {
		val dbResource = withDatabase("mySQL")_
		dbResource(dbWrite)
		dbResource(dbRead)
		// or:
		withDatabase("mySQL") ( mySQLConn => dbWrite(mySQLConn))
		withDatabase("mySQL") ( dbWrite(_) )
		// or:
		withDatabase("ingres") { conn =>
			dbWrite(conn)
			dbRead(conn)
		}
	}
	def dbWrite(con:Connection) {
		println("writing to database: " + con.host)
	}
	def dbRead(con:Connection) {
		println("reading from database: " + con.host)
	}
	def withDatabase(dbName:String)(f: (Connection) => Unit) = {
		val conn:Connection = DriverManager.getConnection(dbName)
		println(dbName + " connection at " + conn.host + " acquired")
		try {
			f(conn)
		} finally {
			if (conn != null) conn.close
		}
	}
	abstract class Connection(val host:String) {
		def close()
	}
	class MySQLConnection(host:String) extends Connection(host) {
		def close() {
			println("MySQL connection at " + host + " closed")
		}
	}
	class IngresConnection(host:String) extends Connection(host) {
		def close() {
			println("Ingres connection at " + host + " closed")
		}
	}
	object DriverManager {
		val connections = Map("mySQL" -> new MySQLConnection("localhost"),
				"ingres" -> new IngresConnection("jarden.com"))
		def getConnection(dbName:String):Connection = connections(dbName)
	}
}

