package jarden.generics

/**
 * Why we might like Scala; major plus points:
 * 		immutable classes allow for covariant collections
 * 		encourages declarative rather than imperative style
 * 			whereas in Java, if it compiles, it is likely to run
 * 			in Scala it is more likely to run properly
 * Minor plus points:
 * 		lots of help from compiler: type inference, class declarations
 * Serious minus point: beyond the intellectual capacity of many programmers
 * @author john.denny@gmail.com
 */
// TODO: have immutable version: add creates new list; tail 
// returns link to 2nd element; and mutable version
// check exceptions; do real classes throw NoSuchElementException?
object MyLinkedList {
	def apply[T](elems: T*): MyLinkedList[T] = {
		if (elems.size == 0) {
			new MyLinkedList[T]()
		} else {
			var current = new Link[T](elems(elems.size - 1))
			for (i <- (elems.size - 2).to(0, -1)) {
				current = new Link[T](elems(i), current)
			}
			new MyLinkedList[T](current, elems.size)
		}
	}
}

class Link[+T](val elem: T, val next: Link[T] = null)

class MyLinkedList[+T](val first: Link[T] = null, val size: Int = 0) {
	private def getLink(index: Int): Link[T] = {
		if (index >= size || index < 0)
			throw new IndexOutOfBoundsException(index.toString)
		var current = first
		for (i <- 0 until index) current = current.next
		current
	}
	def apply(index: Int): T = getLink(index).elem
	def head: T =
		if (first == null) throw new NoSuchElementException()
		else getLink(0).elem
	def tail: MyLinkedList[T] = {
		if (first == null) throw new UnsupportedOperationException(
				"empty collection")
		else new MyLinkedList[T](first.next, size - 1)
	}
	/**
	 * Return new MyLinkedList with elem appended.
	 */
	def :+[S>:T](elem: S) = {
		// my path is certain but my progress is slow...
		val newLast = new Link[S](elem)
		if (this.isEmpty) {
			new MyLinkedList[S](newLast, 1)
		} else {
			var current = new Link[S](getLink(size - 1).elem, newLast)
			for (i <- (size - 2).to(0, -1)) {
				current = new Link[S](getLink(i).elem, current)
			}
			new MyLinkedList[S](current, size + 1)
		}
	}
	/**
	 * Return new MyLinkedList with elem prepended.
	 */
	def +:[S>:T](elem: S) = {
		new MyLinkedList[S](new Link[S](elem, first), size + 1)
	}
	def isEmpty = size == 0
	override def toString = {
		val buffer = new StringBuffer("MyLinkedList(")
		@scala.annotation.tailrec
		def appendNextElem(next: MyLinkedList[T]) {
			buffer.append(next.head)
			if (!next.tail.isEmpty) {
				buffer.append(",")
				appendNextElem(next.tail)
			}
		}
		if (!isEmpty) appendNextElem(this)
		buffer.append(")")
		buffer.toString
	}
}

object MyLinkedListTest extends App {
	val myLL = MyLinkedList("jack", "jill", "joan", "jonny")
	println(myLL)
	assert(myLL.head == "jack")
	println(myLL.tail)
	assert(myLL(0) == "jack")
	assert(myLL(1) == "jill")
	val myLL2 = "dave" +: myLL
	println(myLL2)
	val myLL3 = myLL2 :+ "charles"
	println(myLL3)

	class Fruit
	trait Yellow
	class Apple extends Fruit
	class Bramley extends Apple
	class Banana extends Fruit with Yellow
	val bagFruit = MyLinkedList(new Apple, new Banana)
	val bagApples = MyLinkedList(new Apple, new Bramley)
	val bagFruitA: MyLinkedList[Fruit] = bagApples
	val bagFruitB: MyLinkedList[Fruit] = MyLinkedList(new Banana)
	val bagYellows: MyLinkedList[Yellow] = MyLinkedList(new Banana)
	val resF = new Apple +: bagFruitA // bag of fruit
	val resA = new Apple +: bagApples // bag of apples
	val resF2 = new Banana +: bagFruitA
	val resF3 = new Banana +: bagApples
	
	// testMyArrayList

	println("adios mi amigita")
	
	def testMyArrayList {
		val myList = new MyArrayList[String]
		println(myList)
		myList.add("john")
		println(myList)
		myList.add("julie")
		assert(myList(0) == "john")
		assert(myList(1) == "julie")
		println(myList)
	}
}

// Alternatively, we can wrap java collections:
import java.util.ArrayList

class MyArrayList[T] {
	val list = new ArrayList[T]
	def add(t: T) = list.add(t)
	def apply(index: Int) = list.get(index)
	override def toString = {
		val buffer = new StringBuffer("MyArrayList(")
		for (i <- 0 until list.size) {
			buffer.append(list.get(i))
			if (i < list.size - 1) buffer.append(", ")
		}
		buffer.append(")")
		buffer.toString
	}
}
