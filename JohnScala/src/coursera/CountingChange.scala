package coursera

object CountingChange extends App {
	/*
	Given an amount to produce (e.g. as change), and a
	list of unique denominations for the coins, return
	count of how many different ways to produce amount
	e.g. money is 4, coins are 1 and 2, combinations are
	1+1+1+1, 1+1+2, 2+2, i.e. answer is 3
	 */
	def countChange(money:Int, coins:List[Int]):Int = {
		if (money == 0) 0
		else if (coins.length == 0) 0
		else 1
	}
	/*
	 * e.g. money is 4; coins are 1 and 2
	 Pseudo-code
	 sort coins?
	 1st coin + 1st coin etc until >= money
	 if == money then add to list
	 replace last with next in list; repeat
	 */
}