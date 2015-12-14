package jarden

import scala.collection.mutable.ArrayBuffer

object MyArrays {
	def main(args: Array[String]): Unit = {
		val names = ArrayBuffer("John", "Julie", "Sam", "Joe")
		for (name <- names) println(name)
		names.foreach(println)
		println(names.mkString(":"))
		val doAll = false
		if (doAll) {
			arrayBuff()
			array()
		}
	}
	def array() {
		val days = new Array[String](3)
		days(0) = "Lunes"
		days(1) = "Martes"
		days(2) = "Miercoles"
		for (i <- 0 to days.length - 1) println(days(i))
	}
	def arrayBuff() {
		val names = ArrayBuffer("John", "Julie", "Sam", "Joe")
		names.foreach(arg => println(arg))
		names.update(0, "Jacks")
		names += "Angela"
		names.+=:("Sarai")
		println("final answer?")
		names.foreach(println)
		println("phone a friend?")
		for (name <- names) println(name)
	}
}