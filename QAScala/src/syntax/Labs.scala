package syntax

object Labs extends App {
	def findLeaps(y1: Int, y2: Int) = {
		for {
			year <- y1 to y2
			if year % 4 == 0 &&
			year % 100 != 0 ||
			year % 400 == 0
		} yield year
	}
	def isLeapYear(year: Int) = {
		year % 4 == 0 &&
			(year % 100 != 0 ||
			year % 400 == 0)
	}
	def findLeaps2(y1: Int, y2: Int) =
		Range(y1, y2).filter(year => isLeapYear(year))
		
	def xPn(x: Double, n: Int): Double = {
		if (n == 0) 1
		else if (n == 1) x
		else if (n > 0) {
			if (n % 2 == 0) { // n is even +ve
				val y = xPn(x, n / 2)
				y * y
			} else { // n is odd +ve
				x * xPn(x, n - 1)
			}
		} else { // n is -ve
			1 / xPn(x, -n)
		}
	}
	assert(isLeapYear(1896))
	assert(!isLeapYear(1900))
	assert(isLeapYear(2000))
	println(findLeaps2(1960, 2010))
	val leaps = findLeaps(1960, 2010)
	println(leaps)
	println("2^-2=" + xPn(2, -2))
	println("2^2=" + xPn(2, 2))
	println("2^3=" + xPn(2, 3))
	println("2^5=" + xPn(2, 5))
	assert(xPn(2, 5) == 32)
	assert(xPn(2, 4) == 16)
	assert(xPn(2, -2) == 0.25)
	println("adios mi amigitos")
}