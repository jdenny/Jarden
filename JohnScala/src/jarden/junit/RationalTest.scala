package jarden.junit

import jarden.maths.Rational
import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test

/**
 * @author john.denny@gmail.com
 * see http://alvinalexander.com/scala/how-to-use-junit-testing-with-scala
 * 
 */
class RationalTest extends TestCase {
	@Test
	def testArithmetic() {
		val rat2d5 = Rational(2, 5)
		val rat3d8 = Rational(3, 8)
		assertEquals(rat2d5 + 1, Rational(7, 5))
		assertEquals(rat3d8 * 2, Rational(3, 4))
		assertEquals(rat2d5 / 2, Rational(1, 5))
		assertEquals(rat2d5 - 1, Rational(-3, 5))
		assertEquals(rat2d5 + rat3d8, Rational(31, 40))
		assertEquals(rat2d5 - rat3d8, Rational(1, 40))
		assertEquals(rat2d5 * rat3d8, Rational(3, 20))
		assertEquals(rat2d5 / rat3d8, Rational(16, 15))
		assertEquals(1 + rat2d5, Rational(7, 5))
		assertEquals(2 * rat3d8, Rational(3, 4))
		assertEquals(2 / rat2d5, Rational(5, 1))
		assertEquals(1 - rat2d5, Rational(3, 5))
		assertTrue(rat3d8 < rat2d5)
	}
	@Test
	def testThrowException() {
		try {
			Rational(2, 0)
			fail("didn't throw IllegalArgumentException on zero denominator")
		} catch {
			case e: IllegalArgumentException => // should happen
		}
	}

}