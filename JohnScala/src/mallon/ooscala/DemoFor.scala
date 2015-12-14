package mallon.ooscala

object ForDemo {
	def main(args: Array[String]) {
		(1 to 5).foreach(println(_))
		(1 to 5).foreach(println)
		1 to 5 foreach println
		2.to(5).foreach(i => println(s"$i squared is ${i * i}"))
		for(i <- 3 to 6) println(s"$i squared is ${i * i}")
		4 to 7 foreach (i => println(s"$i squared is ${i * i}"))
		timesTable(6)
	}
	def timesTable(n: Int) {
		1 to 5 foreach(i => println(s"$i x $n = ${i * n}"))
	}
}