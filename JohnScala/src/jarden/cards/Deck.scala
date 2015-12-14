package jarden.cards

import jarden.cards.Deck._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class Deck {

	def deal(playerCt:Int, handCt:Int) = {
		val shuffled = Random.shuffle(pack)
		for(playerNum <- 1 to playerCt) yield
			((1 to handCt * playerCt).filter(cardNum =>
			cardNum % playerCt == (playerNum - 1))).map(i => shuffled(i-1))
	}
}

object Deck {
	val cardsInDeck = 52
	val cardIndices = 1 to cardsInDeck
	val pack:Seq[Card] = (for (s <- Suit.values; r <- Rank.values) yield
			(new Card(s, r))).toSeq

	def main(args: Array[String]): Unit = {
		println(pack)
		val deck = new Deck()
		val players = 4
		val handSize = 13
		val deal = deck.deal(players, handSize)
		println("deal=" + deal)
		for (i <- 1 to players) println("hand(" + (i) + ")=" + deal(i-1) )
	}
	// TODO: show a gradual build-up towards shuffle and deal
	def demo = {
		val cards = 1 to 52
		val shuffled = Random.shuffle(cards)
		println(shuffled)
		val player1 = cards.filter(num => num % 4 == 1).map(x => shuffled(x-1))
		println("player1=" + player1)
		
	}

}



//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Random;
//
//public class CardPack {
//	public final static int DECK_SIZE = 52;
//	public final static int PLAYERS = 4;
//	public final static int HAND_SIZE = DECK_SIZE / PLAYERS;
//	
//
//	private Card[] cards;
//	private Hand[] hands;
//	
//	public CardPack() {
//		cards = new Card[DECK_SIZE];
//		int i = 0;
//		for (Suit suit: Suit.values()) {
//			for (Rank rank: Rank.values()) {
//				cards[i++] = new Card(suit, rank);
//			}
//		}
//	}
//	public Card[] getCards() {
//		return cards;
//	}
//	public void shuffle() {
//		Random random = new Random();
//		Card swap;
//		for (int i = 0; i < DECK_SIZE; i++) {
//			int j = random.nextInt(DECK_SIZE);
//			swap = cards[j];
//			cards[j] = cards[i];
//			cards[i] = swap;
//		}
//	}
//	/**
//	 * Actually, deal and sort. We're good like that.
//	 */
//	public void deal() {
//		hands = new Hand[PLAYERS];
//		ArrayList<Card> cardList;
//		for (int p = 0; p < PLAYERS; p++) {
//			cardList = new ArrayList<Card>();
//			for (int i = 0; i < HAND_SIZE; i++) {
//				cardList.add(cards[i * PLAYERS + p]);
//			}
//			Collections.sort(cardList);
//			hands[p] = new Hand();
//			hands[p].cards = cardList;
//		}
//	}
//	public Hand getHand(Player player) {
//		return hands[player.ordinal()];
//	}
//}
