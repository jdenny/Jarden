package mallon.ooscala

import jarden.maths.Rational

object Lab2Fraction {
	def assertEquals(act:Rational, exp:Rational):Unit = {
		assert(act == exp, "actual=" + act + "; expected=" + exp)
	}
	def assertEquals(act:Int, exp:Int):Unit = {
		assert(act == exp, "actual=" + act + "; expected=" + exp)
	}
	def main(args: Array[String]): Unit = {
		Rational.INFO = false
		assertEquals(8, Rational.getHCF(24, 56))
		val frac3d4 = Rational(3, 4)
		println("frac3d4=" + frac3d4)
		val frac6d8 = Rational(6, 8) 
		println("frac6d8=" + frac6d8)
		val frac9d12 = Rational(9, 12) 
		println("frac9d12=" + frac9d12)
		val frac12d16 = Rational(12, 16) 
		println("frac12d16=" + frac12d16)
		try {
			val frac3d0 = Rational(3, 0) 
			assert(false, "should have thrown exception")
		} catch {
			case iae: IllegalArgumentException => println("exception correctly thrown")
		}
		var res:Rational = frac3d4 + Rational(3, 5)
		println("res=" + res)
		assert(res == Rational(27, 20), "incorrect value of res")
		assert(frac3d4 < Rational(4, 5) && frac3d4 > Rational(2, 3))
		assert(frac6d8 / 3 == Rational(1, 4), "incorrect division")
		assert((Rational(7, 4) - 1) == Rational(3, 4), "incorrect subtraction")
		assert((1 - frac3d4) == Rational(1, 4), "incorrect subtraction")
		res = 5
		assert(res == Rational(5, 1), "incorrect conversion")
		val frac7 = Rational(7)
		assertEquals(frac7, Rational(7, 1))
		assertEquals(frac3d4 - Rational(4, 5), Rational(-1, 20))
		println(Rational(-3, 20))
		println(Rational(-3, -20))
		res = frac3d4 - frac7 / frac3d4 // test precedence of operators
		val expected = Rational(-103, 12)
		assertEquals(res, expected)
		Rational.printCount()
	}

}
