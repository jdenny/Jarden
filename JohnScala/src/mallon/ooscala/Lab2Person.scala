package mallon.ooscala

import jarden.MyDate

object Lab2Person {

	def main(args: Array[String]): Unit = {
		val dob = new MyDate(14, 11, 1951)
		val john = new L2Person("john", "leslie", dob)
		println(s"L2Person[${john.firstName}, ${john.secondName}, ${john.dob}]")
	}

}

class L2Person(val firstName: String, val secondName: String,
		val dob: MyDate = null)

