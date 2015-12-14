package mallon.functional

import java.util.Date

/**
 * @author john.denny@gmail.com
 */
object TimeTrial extends App {
	println("preparing seq")
	val seq = for(i <- 1 to 10000000) yield (i)
	println("seq ready")
	val t1 = (new Date).getTime
	var total = 0
	for (i <- seq) total += 1
	val t2 = (new Date).getTime
	println(s"total=$total; time=${t2-t1}")
	
	@scala.annotation.tailrec
	def getLength(seq: Seq[Int], len: Int = 0): Int = 
		if (seq.isEmpty) len
		else getLength(seq.tail, len + 1)

//	def getLength(seq: Seq[Int], len: Int = 0): Int = seq match {
//		case Nil => len
//		case _ => getLength(seq.tail, len + 1)
//	}
	val t3 = (new Date).getTime
	val total2 = getLength(seq)
	val t4 = (new Date).getTime
	println(s"total2=$total2; time=${t4-t3}")
}