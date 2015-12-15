package jarden.collectionclasses

sealed trait List[+A] {
	override def toString = {
		def _toString(list: List[A], firstElem: Boolean): String = list match {
			case Nil => ")"
			case cons: ::[A] => {
				val prefix = if (firstElem) "JList(" else ", "
				prefix + cons.head + _toString(cons.tail, false)
			}
		}
		this match {
			case Nil => "List()"
			case cons: ::[A] => _toString(this, true)
		}
	}
	
	/**
	 * Return new list prepended with b.
	 */
	def ::[B >: A](b: B) = new ::[B](b, this)
	
	/**
	 * Return new List with 1st n elements removed, or Nil if no more than n in list.
	 */
	def drop(n: Int): List[A] = {
		def _drop(m: Int, list: List[A]): List[A] = list match {
			case cons: ::[A] if m > 0 => _drop(m - 1, cons.tail)
			case cons: ::[A] => list
			case _ => Nil
		}
		_drop(n, this)
	}
	/**
	 * Return new List with rh list appended to lh.
	 */
	def ++[B >: A](that: List[B]): List[B] = {
		def _append(lh: List[A], rh: List[B]): List[B] = lh match {
			case Nil => rh
			case cons: ::[A] => new ::[B](cons.head, _append(cons.tail, rh))
		}
		_append(this, that)
	}
	/**
	 * Return a new List with the elements in reverse order.
	 */
	def reverse(): List[A] = {
		@scala.annotation.tailrec
		def _reverse(to: ::[A], from: List[A]): List[A] = from match {
			case Nil => to
			case cons: ::[A] => _reverse(new ::[A](cons.head, to), cons.tail)
		}
		this match {
			case Nil => Nil
			case cons: ::[A] => {
				_reverse(new ::[A](cons.head, Nil), cons.tail)
			}
		}
	}
	/**
	 * Return a new List[B] where each element is transformed using
	 * supplied function f(A) => B.
	 */
	def map[B](f: (A) => B): List[B] = {
		def _map(listA: List[A]): List[B] = listA match {
			case Nil => Nil
			case cons: ::[A] => new ::[B](f(cons.head), _map(cons.tail))
		}
		_map(this)
	}
	/**
	 * Return a new List[A] containing only the elements that match
	 * the supplied predicate.
	 */
	def filter(p: (A) => Boolean): List[A] = {
		@scala.annotation.tailrec
		def nextMatch(list: List[A]): List[A] = list match {
			case cons: ::[A] if p(cons.head) => cons
			case Nil => Nil
			case cons: ::[A] => nextMatch(cons.tail)
		}
		def _filter(list: List[A]): List[A] = nextMatch(list) match {
			case Nil => Nil
			case cons: ::[A] => new ::[A](cons.head, _filter(cons.tail))
		}
		_filter(this)
	}
	/**
	 * Return a new list of the first n elements, or less if there
	 * are less than n elements in this.
	 */
	def take(n: Int): List[A] = {
		def _take(m: Int, list: List[A]): List[A] = list match {
			case Nil => Nil
			case cons: ::[A] if m < 1 => Nil
			case cons: ::[A] => new ::[A](cons.head, _take(m-1, cons.tail))
		}
		_take(n, this)
	}
	// TODO: calculate this at creation?
	def size: Int = {
		@scala.annotation.tailrec
		def _size(n: Int, list: List[A]): Int = list match {
			case Nil => n
			case cons: ::[A] => _size(n+1, cons.tail)
		}
		_size(0, this)
	}
	def takeRight(n: Int): List[A] = {
		@scala.annotation.tailrec
		def _takeRight(ct: Int, list: List[A]): List[A] = list match {
			case cons: ::[A] if ct > 0 => _takeRight(ct - 1, cons.tail)
			case _ => list
		}
		val removeCt = size - n
		if (removeCt <= 0) this
		else _takeRight(removeCt, this)
	}
}
// Note: a case class implements method: equals(any: Any): Boolean
case object Nil extends List[Nothing]
case class ::[+A](val head: A, val tail: List[A]) extends List[A]

object List {
	def apply[A](items: A*): List[A] = {
		if (items.length == 0) Nil
		else new ::(items.head, apply(items.tail: _*))
	}
}

object MyList extends App {
	val list4 = List(1, 2, 3, 4)
	val list56 = 5 :: 6 :: Nil
	assert(list56 == List(5, 6))
	println("list4=" + list4)
	println("list4.drop(2)=" + list4.drop(2))
	assert(List() == list4.drop(6))
	assert(list4 == list4.drop(0))
	assert(list4 == list4.drop(-1))
	assert(List(4, 5) == List(4, 5))
	assert(list4.size == 4)
	assert(List(3, 4) == list4.drop(2))
	assert(List() == List() ++ List())
	assert(List().size == 0)
	assert(List(5, 6) == List() ++ list56)
	assert(List(5, 6) == list56 ++ List())
	assert(List(1, 5, 6) == List(1) ++ list56)
	assert(List(1, 2, 5, 6) == List(1, 2) ++ list56)
	assert(List(1, 2, 3, 4, 5, 6) == list4 ++ list56)
	println("list4.appended=" + (list4 ++ List(5, 6)))
	println("list4.reverse=" + list4.reverse)
	assert(List(1, 2, 3, 4) == list4) // make sure not changed original
	println("list4.map(_ * 2)=" + list4.map(_ * 2))
	assert(List(2, 4, 6, 8) == list4.map(_ * 2))
	println("list4.map(\"I\" + _)=" + list4.map("I" + _))
	assert(List("I1", "I2", "I3", "I4") == list4.map("I" + _))
	println("list4 evens=" + list4.filter(x => x % 2 == 0))
	assert(List(2, 4) == list4.filter(x => x % 2 == 0))
	val evens = (x: Int) => x % 2 == 0
	assert(List(2, 4, 6) == List(1, 2, 3, 4, 5, 6).filter(evens))
	println("list4.take(3)=" + list4.take(3))
	assert(List(1, 2, 3) == list4.take(3))
	println("list4.take(-1)=" + list4.take(-1))
	println("Nil=" + Nil)
	assert(List() == list4.take(-1))
	assert(list4 == list4.take(5))
	println("list4.takeRight(2)=" + list4.takeRight(2))
	assert(List(3, 4) == list4.takeRight(2))
	assert(list4 == list4.takeRight(5))
	
	println("end of simpleList")
	
}

