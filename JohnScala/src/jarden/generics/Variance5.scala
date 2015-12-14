package jarden.generics

import scala.reflect.ClassTag
import jarden.{Animal,Mammal,Human,Dog,Whale}

/**
 * @author john.denny@gmail.com
 */
object Variance5 extends App {
	val john = new Mammal("john")
	val julie = new Mammal("julie")
	val sam = new Dog("sam")
	val joe = new Dog("joe")
	val bag = Bag5(john, julie)
	println(bag)
	val bag2 = Bag5(sam, joe)
	val bigBag = bag ++ bag2
	println(bigBag)
	println(bigBag.getClass)
}

class Bag5[T: ClassTag](size: Int) {
	private val array = new Array[T](size)
	def apply(i: Int) = array(i)
	def set(i: Int, elem: T) {
		array(i) = elem
	}
	override def toString() = {
		val buffer: StringBuffer = new StringBuffer("Bag(")
		for (e <- array) buffer.append(e + ", ")
		buffer.replace(buffer.length() - 2, buffer.length(), ")")
		buffer.toString()
	}
	def ++ [S <: T](that: Bag5[S])(implicit arg0: ClassTag[S]) = {
		val bag = new Bag5[T](array.size + that.array.size)
		for (i <- 0 until array.size) bag.set(i, array(i))
		for (i <- 0 until that.array.size)
			bag.set(i + array.size, that.array(i))
		bag
	}
}

object Bag5 {
	def apply[T: ClassTag](elems: T*) = {
		val bag = new Bag5[T](elems.size)
		for (i <- 0 until elems.size) bag.set(i, elems(i))
		bag
	}
}