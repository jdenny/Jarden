package mallon.ooscala

object MainObject {
	println("start of MainObject")
	val random = new scala.util.Random
	
	def main(args: Array[String]) {
		println("start of main()")
		println("randVal=" + randVal)
		println("randDef=" + randDef)
		println("randVal=" + randVal)
		println("randDef=" + randDef)
		println("end of main()")
		
	}
	lazy val randVal = {
		println("randVal")
		random.nextInt(100)
	}
	def randDef = {
		println("randDef")
		random.nextInt(100)
	}
	println("end of MainObject")
}