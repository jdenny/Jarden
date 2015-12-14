package mallon.functional

object Lab4Option {

	def main(args: Array[String]): Unit = {
		println("starting Lab4Option")
		assert(divide(28, 7) == Some(4))
		assert(divide(28, 7).get == 4)
		assert(divide(28, 0) == None)
		val numbers = Seq(1, 4, -1, 9)
		val rootOpts = applySqrt(numbers)
		assert(rootOpts == Seq[Option[Double]](Some(1), Some(2), None, Some(3)))
		val roots = rootOpts.flatten
		assert(roots == Seq[Double](1, 2, 3))
		println("ending Lab4Option")
	}
	def divide(a:Int, b:Int):Option[Int] = {
		if (b == 0) None
		else Some(a/b)
	}
	def squareRoot(d:Double):Option[Double] = {
		if (d < 0) None
		else Some(Math.sqrt(d))
	}
	def applySqrt(seq:Seq[Int]) = {
		seq.map(a => squareRoot(a))
	}

}