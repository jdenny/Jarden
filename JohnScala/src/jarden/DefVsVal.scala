package jarden

import scala.util.Random

object DefVsVal {

	def main(args: Array[String]): Unit = {
		val random = new Random()
		println("show difference between val and def. Part 1: assign/calculate ints**************")
		def resD = {
			println("DEBUG resD")
			random.nextInt(100)
		}
		val resV = {
			println("DEBUG resV") // this done only once, when resV first evaluated
			random.nextInt(100)	  // value of resV evaluated once
		}
		println("resV=" + resV)
		println("resV=" + resV)
		println("resD=" + resD)
		println("resD=" + resD)
		
		println("Part 2: assign/calculate functions **************")
		def resAD(a:Int) = {
			val rand = random.nextInt(100)
			println("DEBUG resAD(" + a + "); rand=" + rand)
			a * rand
		}
		val resAV = {
			val rand = random.nextInt(100) // this done only once, when resAV first evaluated
			println("DEBUG resAV(); rand=" + rand) // this only done once, when resAV first evaluated
			(a:Int) => {
				a * rand
			}
		}
		println("resAV(3)=" + resAV(3))
		println("resAV(3)=" + resAV(3))
		println("resAD(3)=" + resAD(3))
		println("resAD(3)=" + resAD(3))
	}
		
}