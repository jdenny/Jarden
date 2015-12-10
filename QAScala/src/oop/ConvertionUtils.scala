package oop

object ConvertionUtils extends App {
	val temperaturesC = Seq(-40, 0, 1, 11, 21, 100)
	val temperaturesF = temperaturesC.map(CToF.from(_)) 
	println(temperaturesF)
	println(temperaturesF.map(f => CToF.to(f)))
	println("buenas vacaciones")
	val conv: Conversions = Feet2Metres
	val lengthsF = Seq(1, 1.5, 2, 3.281)
	val lengthsM = lengthsF.map(conv.from(_))
	println(lengthsM)
	println(lengthsM.map(m => conv.to(m)))
	println("good lengths!")
}

trait Conversions {
	def from(x: Double): Double
	def to(x: Double): Double
}

object CToF extends Conversions {
	def from(x: Double) = x * 9 / 5 + 32
	def to(x: Double) = (x - 32) * 5 / 9
}

object Feet2Metres extends Conversions {
	override def from(x: Double) = x * 12 * 2.54 / 100
	override def to(x: Double) = x * 100 / 12 / 2.54
}