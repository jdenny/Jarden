package mallon.functional

/*
1. Cards, shuffle(), deal(playerCt, cardCt) - see jarden.cards
2. report library program by types (see earlier lab)
3. find all library items overdue
 */
object Lab3Collections {
	val numberOfPlayers = 4

	def main(args: Array[String]): Unit = {
		val deck = Range(1, 14).toList
		println(deck.partition(a => myFilter(a, 1)))
		val hand1 = deck.filter(myFilter(_, 1))
		println(hand1)
		val allHands = for(n <- 1 to 4) yield (deck.filter(myFilter(_, n)))
		println(allHands)
	}
	def myFilter(cardNum:Int, playerNum:Int) = cardNum % numberOfPlayers == (playerNum - 1)
	
//	def deal(playerNum:Int) = {
//		(0 to 11).filter(cardNum => myFilter(cardNum, playerNum)).map(cn => cards(cn))
//	}

}