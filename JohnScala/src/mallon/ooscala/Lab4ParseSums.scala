package mallon.ooscala

object Lab4ParseSums {
	val debug = false

	def main(args: Array[String]): Unit = {
		var line:String = ""
		println("expression of form: a op b or q for quit")
		do {
			println(">")
			line = readLine()
			if (!line.startsWith("q")) {
				try {
					println("result is " + process(line))
				} catch {
					case ex:Exception => println(ex) 
				}
			}
		} while (!line.startsWith("q"))
		
		println("adios mi amigito")
	}
	
	def add(a:Int, b:Int):Int = a + b
	def subtract(a:Int, b:Int):Int = a - b
	def multiply(a:Int, b:Int):Int = a * b
	def power(base:Int, exp:Int):Int = {
		assume(exp >= 0, "exponent must be >= 0")
		var sum = base;
		for (i <- 2 to exp) sum *= base
		sum
	}
	def doSums(func: (Int, Int) => Int, a:Int, b:Int) = func(a, b)
	
	def process(line:String):Int = {
		if (debug) println("line=" + line)
		val tokens = line.split(" ")
		require(tokens.length == 3, "should be 3 tokens in line")
		val a = Integer.parseInt(tokens(0))
		val b = Integer.parseInt(tokens(2))
		val op = tokens(1)
		val f:(Int, Int)=>Int = if (op.equals("add") || op.equals("+")) add
			else if (op.equals("subtract") || op.equals("-")) subtract
			else if (op.equals("multiply") || op.equals("*")) multiply
			else if (op.equals("power") || op.equals("^")) power
			else throw new IllegalArgumentException("unrecognized operator: " + op)
		doSums(f, a, b)
	}

}