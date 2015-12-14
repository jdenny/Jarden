package jarden.generics

/**
 * @author john.denny@gmail.com
 */
class MyTuple2[S, T](val _1: S, val _2: T) {
	def this(tuple2: Tuple2[S, T]) = this(tuple2._1, tuple2._2)
	override def toString = "(" + _1 + ", " + _2 + ")"
	
}

object MyTuple2 {
	def apply[S, T](a: S, b: T) = new MyTuple2(a, b)
}

object TestTuple2 extends App {
	val tup2 = (5, "May")
	val tup2a = (6 -> "June")
	val t2 = new MyTuple2("John", 23)
	val t2a = MyTuple2(6, 36)
	println(t2._1 + ", " + t2._2)
	println(t2)
	val t2b= new MyTuple2("Julie" -> 33)
	println(t2b)
	
	println("adios mi amigita")
}