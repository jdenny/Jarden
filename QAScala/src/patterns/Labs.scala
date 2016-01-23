package patterns

object Labs extends App {
	// basic
	// extractors
	// expressionEvaluation
	simpleList2
}

object basic {
	def nth(list: List[Any], n: Int): Any = n match {
		case 0 => list.head
		case _ => nth(list.tail, n-1)
	}
	
	val nums = List(2, 3, 5, 8, 12)
	assert(nth(nums, 2) == 5)
	assert(nth(nums, 4) == 12)
	val names = List("Thomas", "Richard", "Harold")
	assert(nth(names, 0) == "Thomas")
	assert(nth(names, 2) == "Harold")

	println("end of basic")
}

object extractors {
	class Person(val name: String, val address: Address) {
		override def toString = name
	}
	class Address(val city: String, val country: String) {
		override def toString = city + ", " + country
	}
	
	object Person {
		def apply(name: String, address: Address) =
			new Person(name, address)
		def unapply(person: Person): Option[(String, Address)] =
			Some((person.name, person.address))
	}
	object Address {
		def apply(city: String, country: String) =
			new Address(city, country)
		def unapply(address: Address): Option[(String, String)] =
			Some((address.city, address.country))
	}
	val john = Person("John", Address("Bournemouth", "England"))
	val bill = Person("Bill", Address("New York", "USA"))
	val joe = Person("Joe", Address("London", "England"))
	val team = List(john, bill, joe)
	for (bod <- team) bod match {
		case Person("John", _) => println("it's John")
		case Person("Joe", _) => println("it's Joe")
		case _ => println(bod.name + " is someone new")
	}
	def livesIn(person: Person) = person.address match {
		case Address("London", _) => "London"
		case Address("New York", _) => "New York"
		case _ => "somewhere else"
	}
	for (bod <- team) println(s"${bod.name} lives in ${livesIn(bod)}")
	def livesInEngland(bod: Person) = bod match {
		case Person(_, Address(_, "England")) => true
		case _ => false
	}
	val engTeam = team.filter(livesInEngland(_))
	println("England Team: " + engTeam)
	println("end of extractors")
}

object expressionEvaluation {
	sealed trait Expression
	case class Const(v: Int) extends Expression
	case class Neg(e: Expression) extends Expression
	case class Add(e1: Expression, e2: Expression) extends Expression
	
	def eval(ex: Expression): Int = ex match {
		case Const(v) => v
		case Neg(e) => - eval(e)
		case Add(e1, e2) => eval(e1) + eval(e2)
	}

	// 10 + (-(3 + 4))
	val expr = Add(Const(10), Neg(Add(Const(3), Const(4))))
	val res = eval(expr)
	println(res)
	assert(res == 3)
	
	println("end of expressionEvaluation")
}

object simpleList2 {
	sealed trait JList[+A] {
		override def toString = {
			def _toString(list: JList[A], firstElem: Boolean): String = list match {
				case JNil => ")"
				case cons: JCons[A] => {
					val prefix = if (firstElem) "JList(" else ", "
					prefix + cons.head + _toString(cons.tail, false)
				}
			}
			this match {
				case JNil => "JList()"
				case cons: JCons[A] => _toString(this, true)
			}
		}
		def drop(n: Int) = {
			@scala.annotation.tailrec
			def _drop(list: JList[A], m: Int): JList[A] = list match {
				case JNil => JNil
				case jCons: JCons[A] => m match {
					case 0 => jCons
					case 1 => jCons.tail
					case _ => _drop(jCons.tail, m - 1)
				}
			}
			if (n <= 0) this
			else _drop(this, n)
		}
		/**
		 * from this: (1, ^2), (2, ^3), (3, Nil)
		 * and that: (4, ^5), (5, Nil)
		 * produce: (1, ^2), (2, ^3), (3, ^4), (4, ^5), (5, Nil)
		 */
		def append[B>:A](that: JList[B]): JList[B] = {
			that match {
				case JNil => this
				case thatAsCons: JCons[B] => {
					def _makeTail(next: JList[B]): JCons[B] = next match {
						case JNil => thatAsCons
						case jCons: JCons[A] => new JCons(jCons.head, _makeTail(jCons.tail))
					}
					_makeTail(this)
				}
			}
		}
		/**
		 * From this: (1, ^2), (2, ^3), (3, Nil)
		 * Produce: (1, Nil), (2, ^1), (3, ^2)
		 * and return ^3
		 */
		def reverse(): JList[A] = {
			@scala.annotation.tailrec
			def _reverse(current: JCons[A],
					next: JList[A]): JCons[A] = next match {
				case JNil => current
				case jCons: JCons[A] =>
					_reverse(new JCons[A](jCons.head, current), jCons.tail)
			}
			this match {
				case JNil => JNil // i.e. this
				case jCons: JCons[A] =>
					_reverse(new JCons[A](jCons.head, JNil), jCons.tail)
			}
		}
		def map[B](f: A => B): JList[B] = {
			def _map(jList: JList[A]): JList[B] = jList match {
				case JNil => JNil
				case jCons: JCons[A] =>
					new JCons(f(jCons.head), _map(jCons.tail))
			}
			_map(this)
		}
		/**
		 * Return new list where each element matches predicate p.
		 */
		def filter(p: (A) => Boolean): JList[A] = {
			@scala.annotation.tailrec
			def getNextMatch(jList: JList[A]): JList[A] = jList match {
				case jCons: JCons[A] if p(jCons.head) => jCons
				case JNil => JNil
				case jCons: JCons[A] => getNextMatch(jCons.tail)
			}
			def _filter(jList: JList[A]): JList[A] =
					getNextMatch(jList) match {
				case JNil => JNil
				case jCons: JCons[A] =>
					new JCons[A](jCons.head, _filter(jCons.tail))
			}
			_filter(this)
		}
		/**
		 * Return new list containing the first n items of this list
		 */
		def take(n: Int): JList[A] = {
			def _take(n: Int, jList: JList[A]): JList[A] = jList match {
				case jCons: JCons[A] if n > 0 => new JCons(jCons.head, _take(n-1, jCons.tail))
				case _ => JNil // stop when n runs out or we reach JNil
			}
			_take(n, this)
		}
	}
	case object JNil extends JList[Nothing]
	case class JCons[+A] (head: A, tail: JList[A]) extends JList[A]
	
	val jlist = new JCons[String]("Hola", JNil)
	val jlist2 = new JCons[String]("Adiós", jlist)
	val jlist3 = new JCons[String]("Martes", new JCons[String]("Lunes", JNil))
	
	object JList {
		def apply[T](items: T*): JList[T] = {
			if (items.length == 0) JNil
			else new JCons(items.head, apply(items.tail: _*))
		}
	}
	val jlist4 = JList(1, 2, 3, 4)
	val jlist56 = JList(5, 6)
	println("jlist4=" + jlist4)
	println("jlist4.drop(2)=" + jlist4.drop(2))
	assert(JList() == jlist4.drop(6))
	assert(jlist4 == jlist4.drop(0))
	assert(jlist4 == jlist4.drop(-1))
	assert(JList(3, 4) == jlist4.drop(2))
	assert(JList() == JList().append(JList()))
	assert(JList(5, 6) == JList().append(jlist56))
	assert(JList(5, 6) == jlist56.append(JList()))
	assert(JList(1, 5, 6) == JList(1).append(jlist56))
	assert(JList(1, 2, 5, 6) == JList(1, 2).append(jlist56))
	assert(JList(1, 2, 3, 4, 5, 6) == jlist4.append(jlist56))
	println("jlist4.appended=" + jlist4.append(JList(5, 6)))
	println("jlist4.reverse=" + jlist4.reverse)
	println("jlist4=" + jlist4)
	assert(JList(2, 4, 6, 8) == jlist4.map(_ * 2))
	println("jlist4.map(\"I\" + _)=" + jlist4.map("I" + _))
	assert(JList("I1", "I2", "I3", "I4") == jlist4.map("I" + _))
	println("jlist4 evens=" + jlist4.filter(x => x % 2 == 0))
	assert(JList(2, 4) == jlist4.filter(x => x % 2 == 0))
	val evens = (x: Int) => x % 2 == 0
	assert(JList(2, 4, 6) == JList(1, 2, 3, 4, 5, 6).filter(evens))
	println("jlist4.take(3)=" + jlist4.take(3))
	assert(JList(1, 2, 3) == jlist4.take(3))
	println("jlist4.take(-1)=" + jlist4.take(-1))
	println("JNil=" + JNil)
	assert(JList() == jlist4.take(-1))
	assert(jlist4 == jlist4.take(5))
	
	println("end of simpleList")
}