package jarden.functional

object PartiallyDefined {
	val capitals = Map(("England", "London"), ("France", "Paris"))
	
	def main(args: Array[String]) {
		assert(capitals("England") == "London")
		try {
			assert(capitals("Ecuador") == "Quito")
			println("that's odd!")
		} catch {
			case nse: NoSuchElementException => println("that's good")
		}
		def orElseF = (s:String) => "no sabemos el capital de " + s
		assert(capitals.applyOrElse("England", orElseF) == "London")
		// assert(capitals.applyOrElse("England", orElseF) == "London")
		val res = capitals.applyOrElse("Ecuador", orElseF)
		println("res=" + res)
		val capsLock = new GetCapital
		assert(capsLock("England") == "London")
		val res2 = capsLock("Ecuador")
		println(res2)
		assert(!jSqrt.isDefinedAt(-3))
		assert(jSqrt.isDefinedAt(4))
		val res3 = jSqrt(36)
		println("res3=" + res3)
		val capitals2 = capitals.orElse(UnknownCountry)
		println(capitals2("France"))
		println(capitals2("Italy"))

		println("\nAdios mi amigo")
	}
		
	class GetCapital extends PartialFunction[String, String] {
		def isDefinedAt(country:String) = capitals.isDefinedAt(country)
		def apply(country:String) =
			capitals.get(country) match {
				case p:Some[String] => p.get
				case None => "no sabemos el capital de " + country
			}
	}
	object UnknownCountry extends PartialFunction[String, String] {
		def isDefinedAt(country:String) = false
		def apply(country:String) = "no sabemos el capital de " + country
	}
	object jSqrt extends PartialFunction[Double, Double] {
		def isDefinedAt(x:Double) = x >= 0
		def apply(x:Double) = math.sqrt(x)
	}
}