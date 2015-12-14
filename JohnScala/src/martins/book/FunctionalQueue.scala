package martins.book

import jarden.{Animal,Mammal,Human,Dog}

object FunctionalQueue extends App {
	println("start of main")
	val queue = KQueue(1, 2, 3)
	println(queue)
	val queue4 = queue.append(4)
	println(queue4)
	println("queue4.head()=" + queue4.head)
	assert(queue4.head == 1)
	println("queue4.tail=" + queue4.tail)
	// assert(queue4.tail == new KQueue(List(2, 3, 4)))
	val manQ:KQueue[Human] = KQueue(new Human("jack"))
	val mammalQ:KQueue[Mammal] = manQ.append(new Dog("rover"))
	val animalQ:KQueue[Animal] = manQ.append(new Dog("rover"))
	println("animalQ=" + animalQ)
	// val humanQ:KQueue[Human] = manQ.append(new Dog("rover")) // no!
	// val dogQ:KQueue[Dog] = manQ.append(new Dog("rover")) // no!

	println("\nAdios mi amigo")
  
}

trait KQueue[+T] {
	def head:T
	def tail:KQueue[T]
	def append[U >: T](elem:U):KQueue[U]
}

object KQueue {
	private class KQueueImpl[+T] (
			private[this] var leading:List[T],
			private[this] var trailing:List[T]) extends KQueue[T] {
		def mirror =
			if (leading.isEmpty) {
				leading = trailing.reverse
				trailing = Nil
			}
		def head:T = {
	//		if (!leading.isEmpty) leading.head
	//		else trailing.reverse.head
			mirror
			leading.head
		}
		def tail:KQueue[T] = {
	//		if (leading.isEmpty) new KQueue[T](trailing.reverse.tail, List())
	//		else new KQueue[T](leading.tail, trailing)
			mirror
			new KQueueImpl(leading.tail, trailing)
		}
		def append[U >: T](elem:U):KQueue[U] = {
			new KQueueImpl[U](leading, elem::trailing)
		}
		override def toString() = {
			val buffer = new StringBuffer("KQueue(")
			leading.foreach(item => buffer.append(item + ", "))
			trailing.reverse.foreach { item => buffer.append(item + ", ") }
			buffer.replace(buffer.length() - 2, buffer.length(), ")")
			buffer.toString()
		}
	}
	def apply[T](elems: T*):KQueue[T] = new KQueueImpl(elems.toList, Nil)
}
