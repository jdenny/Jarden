package coursera

object Parentheses extends App {
	println("starting Parentheses")
	def balance(chars: List[Char]): Boolean = {
		def helper(chars:List[Char], lefts:Int):Boolean = {
			if (chars.isEmpty) {
				lefts == 0
			} else {
				val ch:Char = chars.head
				if (ch == '(') helper(chars.tail, lefts + 1)
				else if (ch == ')') {
						if (lefts < 1) false
						else helper(chars.tail, lefts - 1)
				} else helper(chars.tail, lefts)
			}
		}
		helper(chars, 0)
	}
	val samplesGood = List( "(if (zero? x) max (/ 1 x))",
			"I told him (that it’s not (yet) done). (But he wasn’t listening)" )
	val samplesBad = List(":-)", "())(")
	for (s <- samplesGood) assert(balance(s.toList))
	for (s <- samplesBad) assert(!balance(s.toList))
	println("ending Parentheses")
}