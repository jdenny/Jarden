package mallon.ooscala

import java.util.Date
import java.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.MutableList

object Lab5Collections {

	def main(args: Array[String]): Unit = {
		// ex1()
		ex2()
	}
	def ex1() = {
		val mySeq = List(1, 2, 3, 4, 5, 6, 7, 8)
		val res = mySeq.slice(0, 2) ++ mySeq.drop(3)
		println("res=" + res)
		println("dropElement(mySeq, 2)=" + dropElement(mySeq, 2))

		val myMSeq = MutableList(21, 22, 23, 24)
		myMSeq += 26
		myMSeq.+=:(19)
		println("myMSeq=" + myMSeq)
		println("dropElement(myMSeq, 2)=" + dropElement(myMSeq, 2))
	}
	def timeIt(f:()=>Seq[Int], iters:Int) = {
		val startTime = new Date().getTime()
		for (i <- 1 to iters) {
			val res = f()
			assert(res.length == 6, "Should be 6 numbers, but there are " + res.length)
			assert(!(res.exists(x => x < 0 || x > 49)), "numbers should be from 1 to 49")
			for (i <- 0 to res.length - 1) {
				// assert (!(res.drop(i+1).exists(x => x == res(i))), "duplicate found")
				if (res.drop(i+1).exists(x => x == res(i))) {
					println("rogue res: " + res)
					throw new Exception("duplicate found")
				}
			}
		}
		new Date().getTime() - startTime
	}
	def ex2() {
		println("duration of ex2b=" + timeIt(ex2b, 1000))
		println("duration of ex2a=" + timeIt(ex2a, 1000))
		println("duration of ex2a=" + timeIt(ex2b, 1000))
		println("duration of ex2b=" + timeIt(ex2a, 1000))
		println("6 sorted random unique numbers from 1 to 49")
		val res = ex2a()
		println("  " + res)
	}
	def ex2a():Seq[Int] = {
		val numberPool = new ArrayBuffer[Int]
		for (i <- 1 to 49) numberPool += i
		val numbers = new ArrayBuffer[Int]
		val random = new Random()
		for (i <- 1 to 6) {
			val index = random.nextInt(numberPool.length)
			numbers += numberPool(index)
			numberPool.remove(index)
		}
		numbers.sorted
	}
	def ex2b():Seq[Int] = {
		val numbers = new ArrayBuffer[Int]
		val random = new Random()
		for (i <- 1 to 6) {
			var num = -1
			do {
				 num = random.nextInt(49) + 1
			} while (numbers.exists(x => x == num))
			numbers += num
		}
		numbers.sorted
	}
	// allows duplicates!
	def ex2c():Seq[Int] = {
		val numbers = new ArrayBuffer[Int]
		val random = new Random()
		for (i <- 1 to 6) {
			numbers += random.nextInt(49) + 1
		}
		numbers.sorted
	}
	def dropElement(list:Seq[Int], index:Int) =
		list.slice(0, index) ++ list.drop(index+1);
}
