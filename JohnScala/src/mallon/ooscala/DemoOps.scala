package mallon.ooscala

object DemoOps {
	def main(args: Array[String]) {
		println("start of main()")
		val rap42 = IRap.apply(42)
		println(rap42 toString)
		val rap63 = rap42 + new IRap(21)
		val rap53 = rap42 + 11
		val rapM42 = -rap42
		println("rap63=" + rap63)
		println("rap53=" + rap53)
		println("rap42=" + rap42)
		println("rap63 == new IRap(63)? " + (rap63 == new IRap(63)))
		

        println("\nAdios mi amigita")
	}
}

class IRap(val n:Int) {
	def +(that: IRap) = new IRap(n + that.n)
	def +(m: Int) = new IRap(n + m)
	def unary_- = new IRap(-n)
	override def toString() = "IRap(" + n + ")"
}
object IRap {
	def apply(n:Int) = new IRap(n)
	
}