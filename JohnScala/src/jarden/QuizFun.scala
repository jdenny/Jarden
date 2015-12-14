package jarden

import util.Random

object QuizFun {

	def main(args: Array[String]): Unit = {

	}

}

object QA {
	def getNextQuestion() = {
		val randomNum: Random = new Random()
		val opCode = randomNum.nextInt(4)
		val f:(Int, Int)=>Int = opCode match {
			case 0 => add
			case 1 => subtract
			case 2 => multiply
			case 3 => divide
		}
	}
	def add(a:Int, b:Int) = a + b
	def subtract(a:Int, b:Int) = a - b
	def multiply(a:Int, b:Int) = a * b
	def divide(a:Int, b:Int) = a / b
}
class QA (val question:String, val answer:Int) {
	def isCorect(attempt:Int) = this.answer == attempt
}