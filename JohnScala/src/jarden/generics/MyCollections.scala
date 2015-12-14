package jarden.generics

import scala.reflect.ClassTag
import jarden.collectionclasses.List

/* Scala collections: key classes
 * 							<- Set (no duplicates)
 * 							<- Map
 * Traversable <- Iterable  <- Seq <- LinearSeq <- List
 * (foreach)      (iterator)   (ordered)
 * 
 */

object MyCollections {
	def main(args:Array[String]) {
		val jc = JCollect(1, 4, 9, 25)
		assert (jc(1) == 4)
		jc(1) = 5
		println(jc)
		assert(jc == JCollect(1, 5, 9, 25))
		val jcs = JCollect("one", "four", "nine", "twenty-five")
		println(jcs)
		println(jcs.reverse)
		println("jcs.head=" + jcs.head)
		println("jcs.tail=" + jcs.tail)
		assert(jc.count(5 < ) == 2)
		assert(jc.count(el => el > 10) == 1)
		assert(jc :+ 36 == JCollect(1, 5, 9, 25, 36))
		assert(36 +: jc == JCollect(36, 1, 5, 9, 25))
		assert(JCollect().headOption == None)
		assert(jcs.headOption == Option("one"))
		
		println("\nadios mi amigito")
	}
	// following defs not used here; just for demonstration
	def mkArray[T](elems: T*)(implicit tag: ClassTag[T]) = {
		println("tag=" + tag)
		Array[T](elems: _*)
	}
	def mkArray3[T](elem: T, size: Int)(implicit tag: ClassTag[T]) = {
		println("tag=" + tag)
		new Array[T](size)
	}
	def mkList[T](elems: T*)(implicit tag: ClassTag[T]) = {
		println("tag=" + tag)
		List[T](elems: _*)
	}
	def mkList2[T](elems: T*) = {
		val list = List[T](elems: _*)
		println("list.getClass=" + list.getClass)
		list
	}
}

class JCollect[T: ClassTag](len:Int) {
	val elements = new Array[T](len)
	
	override def toString() = {
		val sb = new StringBuilder("JCollect(")
		for (i <- 0 until elements.length)
			sb.append(elements(i) + ", ")
		val sbLen = sb.length
		sb.replace(sbLen - 2, sbLen, "")
		sb.append(")")
		sb.toString()
	}
	def apply(n:Int) = elements(n)
	def update(n:Int, value:T) =
		elements(n) = value
	def length:Int = this.elements.length
	def reverse = {
		val len = elements.length
		val jc = new JCollect(len)
		for(i <- 0 until len)
			jc.elements(i) = this.elements(len - 1 - i)
		jc
	}
	def tail =
		if (elements.isEmpty) throw new NoSuchElementException()
		else {
			val len = elements.length
			val jc = new JCollect(len - 1)
			for(i <- 1 until len)
				jc.elements(i - 1) = this.elements(i)
			jc
		}
	def head:T = 
		if (elements.isEmpty) throw new NoSuchElementException()
		else this.elements(0)
	def headOption:Option[T] =
		if (elements.isEmpty) None
		else Option(this.elements(0))
	def count(f:(T)=>Boolean):Int = {
		var ct = 0
		for (el <- elements) if (f(el)) ct += 1
		ct
	}
	override def equals(that:Any) = that match {
		case thatJC:JCollect[T] => this.elements.sameElements(thatJC.elements)
		case _ => false
	}
	override def hashCode() = this.elements.hashCode()
	def :+(el:T) = {
		val len = elements.length
		val jc = new JCollect(len + 1)
		for(i <- 0 until len)
			jc.elements(i) = this.elements(i)
		jc.elements(len) = el
		jc
	}
	def +:(el:T) = {
		val len = elements.length
		val jc = new JCollect(len + 1)
		jc.elements(0) = el
		for(i <- 0 until len)
			jc.elements(i+1) = this.elements(i)
		jc
	}
}

object JCollect {
	def apply[T: ClassTag](elems:T *) = {
		val jc = new JCollect(elems.length)
		for(i <- 0 until elems.length) jc.elements(i) = elems(i)
		jc
	}

}