package test

import org.scalatest.{FlatSpec, Matchers}

import syntax.Labs.isLeapYear

class LeapYearSpec extends FlatSpec with Matchers {
	"function isLeapYear(year)" should
		"return true if year is a leap-year" in {
		isLeapYear(1896) should be (true)
		isLeapYear(1896) should be (true)
		isLeapYear(2016) should be (true)
	}
	it should
		"return false is year is not a leap-year" in {
		isLeapYear(1900) should be (false)
		isLeapYear(2014) should be (false)
	}
}
