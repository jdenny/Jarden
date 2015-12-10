package test

import scala.collection.mutable.Stack
import org.scalatest.FlatSpec
import org.scalatest.Assertions.assert

class SimpleTest extends FlatSpec {
	"A Stack" should "pop values in LIFO order" in {
		val stack = new Stack[Int]
		stack.push(1)
		stack.push(2)
		assert(stack.pop() === 2)
		assert(stack.pop() == 1)
	}
	val s = "Hello"
	intercept[IndexOutOfBoundsException] {
		s.charAt(-1)
		assert(false, "didn't throw exception!")
	}
}

object John {
	def j {
		val fs = new FlatSpec
			
	}
}

