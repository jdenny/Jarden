package jarden.maths

import Math.abs
import scala.annotation.tailrec
import jarden.MyOrd

class Rational (number:Int, denom:Int) extends MyOrd {
	require(denom != 0, "fraction denominator cannot be zero")
	private val hcf = Rational.getHCF(abs(number), abs(denom))
	val num = number / hcf
	val den = denom / hcf
	
	def this(number:Int) = this(number, 1)
	override def toString() = 
		if (this.den == 1) "(" + this.num + ")"
		else "(" + this.num + "/" + this.den + ")"
	override def equals(other:Any) =
		other match {
			case that:Rational => that.num == this.num && that.den == this.den
			case _ => false
		}
	override def hashCode() = 41 * (41 + num) + den
		// or: num.hashCode() ^ den.hashCode()
	def unary_-(): Rational =
		new Rational(-this.num, this.den)
	def +(that:Rational): Rational =
		new Rational(num*that.den + that.num*den, den*that.den)
	def +(n:Int) = new Rational(this.num + n * this.den, this.den)
	def -(that:Rational): Rational =
		new Rational(num*that.den - that.num*den, den*that.den)
	def -(n:Int) = new Rational(this.num - n * this.den, this.den)
	def *(that:Rational): Rational = new Rational(num*that.num, den*that.den)
	def *(n:Int) = new Rational(this.num * n, this.den)
	def /(that:Rational): Rational = new Rational(num*that.den, den*that.num)
	def /(n:Int) = new Rational(this.num, this.den * n)
    def < (other: Any): Boolean = other match {
		case that:Rational => this < that
		case _ => false
	}
    def < (that: Rational): Boolean =
    	this.num * that.den < that.num * this.den
    def apply():Unit = println(this)
}

object Rational {
	var INFO:Boolean = true
	var count = 0 // to monitor how many times getHCF is called
	def apply(n:Int, d:Int) = new Rational(n, d)
	def apply(n:Int) = new Rational(n, 1)
	implicit def intToFraction(i:Int) = {
		if (INFO) println("INFO: intToFraction(" + i + ")")
		new Rational(i, 1)
	}
	def getHCF(a:Int, b:Int) = {
		getHCFHybrid(a, b)
	}
	// my feeble attempt, after 14 years as a Java programmer!
	// val getHCF: (Int, Int) => Int = getHCFJohn
	@tailrec
	def getHCFJohn(a:Int, b:Int):Int = {
		count += 1
		require(a > 0 && b > 0, "both numbers must be > zero")
		if (a == 1) a
		else if (b == 1) b
		else if (a == b) a
		else if (a > b) getHCF(b, a - b)
		else getHCFJohn(a, b - a)
	}
	@tailrec
	def getHCFBook(a:Int, b:Int):Int = {
		count += 1
		if (b == 0) a else getHCFBook(b, a % b)
	}
	@tailrec
	def getHCFHybrid(a:Int, b:Int):Int = {
		count += 1
		if (b == 0 || a == b || a == 1) a
		else if (b == 1) b
		else getHCFHybrid(b, a % b)
	}
	def printCount() = println("gcd count = " + count)
}