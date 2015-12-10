package generics

object Labs extends App {
	// removeItem
	// usingOptions
	// usingMutableMap
	// usingImmutableMap
	simpleList
}

object removeItem {
	val names = List("John", "Julie", "Jackie", "Joe")
	def removeItem(list: List[Any], index: Int) = {
		if (index < 0 || index > list.size) list
		else {
			val split = list.splitAt(index)
			split._1 ++ split._2.tail
		}
	}
	assert(List("John", "Jackie", "Joe") == removeItem(names, 1))
	assert(List("John", "Julie", "Jackie", "Joe") == removeItem(names, 5))
	println("end of removeItem")
}

object usingOptions {
	class Person(val id:Int, val name: String, val age: Int,
			val gender: Option[String])
	object Employees {
		val p2 = new Person(2, "jill", 45, Some("Female"))
		private val people = Map(
				1 -> new Person(1, "jack", 54, Some("Male")),
				2 -> p2)
		def findById(id: Int): Option[Person] = people.get(id)
	}
	assert(None == Employees.findById(3))
	val bod = Employees.findById(2)
	assert(bod.nonEmpty && bod.get == Employees.p2)

	println("end of usingOptions")
}

object usingImmutableMap {
	var freqMap = Map[String, Int]()
	def process(word: String) {
		val optCt = freqMap.get(word)
		optCt match {
			case None => freqMap = freqMap + ((word, 1))
			case si: Some[Int] => freqMap = freqMap.updated(word, si.get + 1)
		}
	}
	val in = new java.util.Scanner(new java.io.File("docs/simple.txt"))
	while (in.hasNext) process (in.next)
	in.close()
	for ((word, ct) <- freqMap) println(s"$word : $ct")
	
	println("end of usingScalaMap")
}

object usingMutableMap {
	import scala.collection.mutable.Map
	val freqMap = Map[String, Int]()
	def process(word: String) {
		val optCt = freqMap.get(word)
		optCt match {
			case None => freqMap += ((word, 1))
			case si: Some[Int] => freqMap.update(word, si.get + 1)
		}
	}
	val in = new java.util.Scanner(new java.io.File("docs/simple.txt"))
	while (in.hasNext) process (in.next)
	in.close()
	for ((word, ct) <- freqMap) println(s"$word : $ct")
	
	println("end of usingScalaMap")
}

object simpleList {
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
	
	println("end of simpleList")
}