package jarden

object MyIter {
	
	def main(args: Array[String]): Unit = {
		val bag = new Bag("john", "peet", "sally")
		val iter = bag.iterator
		while (iter.hasNext) println(iter.next)
		bag.foreach(println)
	}
}

class Bag2(val names:String *) extends Traversable[String] {
	def foreach[U](f:(String)=> U):Unit = names.foreach(f) // or names.foreach(s => f(s))
}

class Bag(val names:String *) extends Iterable[String] {
	def iterator() = new BagIterator(this)
	override def size = names.size
}

class BagIterator(bag:Bag) extends Iterator[String] {
	private var position = 0
	def hasNext() = position < bag.size
	def next() = {
		val name = bag.names(position)
		position += 1
		name
	}
}
