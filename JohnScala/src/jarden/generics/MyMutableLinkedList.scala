package jarden.generics

/**
 * @author john.denny@gmail.com
 */
class MyMutableLinkedList[T] {
	private class Link(val elem: T, var next: Link = null)
	private var first: Link = null
	private var size: Int = 0
	def add(elem: T) {
		if (first == null) {
			first = new Link(elem, null)
		} else {
			val last = getLink(size - 1)
			val newLink = new Link(elem, null)
			last.next = newLink
		}
		size += 1
	}
	private def getLink(index: Int): Link = {
		if (index >= size || index < 0)
			throw new IllegalArgumentException("element " + index + " does not exist")
		var current = first
		for (i <- 0 until index) {
			current = current.next
		}
		current
	}
	def apply(index: Int): T = getLink(index).elem
	def head: T = getLink(0).elem
	def tail: MyMutableLinkedList[T] = {
		if (first == null) throw new UnsupportedOperationException(
				"empty collection")
		val that = new MyMutableLinkedList[T]
		// TODO: fix the commented-out line
		val link1 = getLink(1)
		// that.first = link1
		that.size = this.size - 1
		that
	}
	override def toString = {
		val buffer = new StringBuffer("MyMutableLinkedList(")
		var current = first
		for (i <- 0 until size) {
			buffer.append(current.elem)
			if (i < size - 1) buffer.append(", ")
			current = current.next
		}
		buffer.append(")")
		buffer.toString
	}
}

object MyMutableLinkedListTest extends App {
	val myLL = new MyMutableLinkedList[String]
	println(myLL)
	myLL.add("jack")
	println(myLL)
	myLL.add("jill")
	println(myLL)
	assert(myLL(0) == "jack")
	assert(myLL(1) == "jill")
	testMyArrayList

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