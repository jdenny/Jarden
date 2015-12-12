package patterns

object Labs extends App {
	// basic
	// extractors
	// expressionEvaluation
	simpleList2
}

object basic {
	// simple way:
	def get2(index: Int, list: List[String]) = list(index)
	// using recursion:
	def get(index: Int, list: List[String]) = {
		def _get(list: List[String], count: Int): String = {
			if (index == count) list.head
			else _get(list.tail, count+1)
		}
		_get(list, 0)
	}
	
	val list = List("zero", "uno", "dos", "tres", "cuatro")
	println("get(2, list)=" + get(2, list))
	assert("dos" == get(2, list))
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

	val expr = Add(Const(10), Neg(Add(Const(3), Const(4))))
	println(eval(expr))
}

object simpleList2 {
	sealed trait JList[+A] {
		override def toString() = {
			val builder = new StringBuilder("JList(")
			def _toString(jl: JList[A]): Unit = {
				if (jl == JNil) {
					val end = builder.size
					builder.replace(end - 2, end, ")")
				}
				else {
					val jcon = jl.asInstanceOf[JCons[A]]
					builder ++= (jcon.head + ", ")
					_toString(jcon.tail)
				}
			}
			_toString(this)
			builder.toString()
		}
		def drop(n: Int) = {
			def _drop[A](list: JList[A], n: Int): JList[A] = list match {
				case JNil => JNil
				case _ if n > 0 => _drop(list.asInstanceOf[JCons[A]].tail, n - 1)
				case _ => list
			}
			_drop(this, n)
		}
		def append[B>:A](list2: JList[B]): JList[B] = {
			def _append(list1: JList[A], list2: JList[B]): JList[B] = list1 match {
				case JNil => list2
				case thisAsCons: JCons[A] => _append(
						thisAsCons.tail, new JCons(thisAsCons.head, list2))
			}
			_append(this, list2)
		}
		def reverse(): JList[A] = {
			def _reverse(jCons: JCons[A]): JCons[A] = {
				jCons // this bit needs some work!
			}
			this match {
				case JNil => JNil
				case jCons: JCons[A] => _reverse(jCons)
			}
		}
		private def getLast: A = {
			def _getLast(jCons: JCons[A]): A = jCons.tail match {
				case JNil => jCons.head
				case jCons2: JCons[A] => _getLast(jCons2)
			}
			this match {
				case JNil => throw new IllegalStateException("no elements")
				case _ => _getLast(this.asInstanceOf[JCons[A]])
			}
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
	println("jlist4=" + jlist4)
	println("jlist4.drop(2)=" + jlist4.drop(2))
	println("jlist4.appended=" + jlist4.append(JList(5, 6)))
	
	println("end of simpleList")
}