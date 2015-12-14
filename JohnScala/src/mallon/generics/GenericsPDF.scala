package mallon.generics

object GenericsPDF {
	def main(args: Array[String]) {
		showArrayToSeq
	
		println("\nadios mi amigita")
	}
	def showArrayToSeq {
		val array: Array[String] = Array("john", "julie")
		println("array: " + array)
		f(array) // note annotation in margin
	}
	def f(seq: Seq[String]) { println("seq=" + seq) }
	
}