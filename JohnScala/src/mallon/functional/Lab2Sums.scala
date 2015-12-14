package mallon.functional

import scala.util.{Try, Success, Failure}
import jarden.maths.DoSums


/*
write a function that takes as its arguments a String value describing
an operation to be carried out on a pair of integers. E.g. "add", "subtract",
"power". Return a function that will perform the required operation on
two Ints, returning an Int result. 

Note: most of the solution is in jarden.maths.DoSums
 */
object Lab2Sums {

	def main(args: Array[String]): Unit = {
		println("supply arithmetic expressions in form: func a b, e.g. multiply 5 4")
		println("or in form a op b, e.g. 3 - 5")
		while (true) {
			val line:String = readLine("> ")
			val tryRes = Try { DoSums.evaluateArithmeticExpression(line) }
			tryRes match {
				case Success(res) => println("result=" + res)
				case Failure(ex) => println(ex)
			}
		}
	}

}