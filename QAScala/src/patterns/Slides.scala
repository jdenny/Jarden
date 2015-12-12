package patterns

object Slides extends App {
	// slide4
	slide8
}

object slide4 {
	def test(a: Any) = a match {
		case i: Int => s"$i is an Int"
		case s: String => s"$s is a String"
		case _ => s"$a is a ${a.getClass}"
	}
	println(test(5))
	println(test("5"))
	println(test(List(5)))
}

object slide8 {
	def sum(list: List[Int]) = {
		def _sum(subList: List[Int], total: Int): Int = subList match {
			case Nil => total
			case _ => _sum(subList.tail, total + subList.head)
			// or, as in slide:
			// case head::tail => _sum(tail, total + head)
		}
		_sum(list, 0)
	}
	val nums = List(5, 3, 1, 2, 4)
	println("sum=" + sum(nums))
	// note: of course this is purely for illustration;
	// a much more succinct and obvious solution is:
	println("sum is also: " + nums.sum)
}