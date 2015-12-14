package jarden.cards

object Suit extends Enumeration {
	type Suit = Value
	val Spade, Heart, Diamond, Club = Value
}

object Rank extends Enumeration {
	type Rank = Value
	val Ace, King, Queen, Jack, R10, R9, R8, R7, R6, R5, R4, R3, R2 = Value
}

import Suit._
import Rank._

object Card {
	val ICON_SPADE = '\u2660'
	val ICON_HEART_HOLLOW = '\u2661'
	val ICON_DIAMOND_HOLLOW = '\u2662'
	val ICON_CLUB = '\u2663'
	val ICON_HEART = '\u2665'
	val ICON_DIAMOND = '\u2666'

	def main(args: Array[String]): Unit = {
		val h3 = Card(Suit.Heart, Rank.R3)
		println("h3=" + h3 + " (" + h3.longString + ")")
		for (s <- Suit.values; r <- Rank.values) println(new Card(s, r))
	}
	def apply(suit:Suit, rank:Rank) = new Card(suit, rank)
}

class Card(val suit:Suit, val rank:Rank) /*extends Comparable[Card]*/ {
	private val shortStr = {
		val rs = rank.toString()
		(if (rs.charAt(0) == 'R') rs.substring(1) else rs.substring(0, 1)) + 
			suit.toString().substring(0, 1)
	}
	val longString = rank.toString() + " " + suit.toString()

	override def toString() = shortStr
	def compareTo(card:Card):Int = {
		val compResult:Int = this.suit.id - card.suit.id
		if (compResult == 0) {
			this.rank.id - card.rank.id
		}
		compResult
	}
	override def equals(other:Any):Boolean = other match {
		case card:Card => this.rank.equals(card.rank) &&
			this.suit.equals(card.suit)
		case _ => false
	}
}
