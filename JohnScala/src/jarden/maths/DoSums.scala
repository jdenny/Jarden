package jarden.maths

object DoSums {
	type Arith = (Int, Int) => Int
	val add:Arith = _ + _ //(a, b) => a + b
	val subtract:Arith = (a, b) => a - b
	val multiply:Arith = (a, b) => a * b
	val divide:Arith = (a, b) => a / b
	
	def getArith(op:String):Arith =
		op match {
		case "+" | "add"=> add
		case "-" | "subtract" => subtract
		case "*" | "multiply" => multiply
		case "/" | "divide" => divide
	}

	def evaluateArithmeticExpression(line:String) = {
		val tokens = line.split(" ")
		try {
			val a = tokens(0).toInt
			getArith(tokens(1))(a, tokens(2).toInt)
		} catch {
			case nfe:NumberFormatException => {
				getArith(tokens(0))(tokens(1).toInt, tokens(2).toInt)
			}
		}
	}
}