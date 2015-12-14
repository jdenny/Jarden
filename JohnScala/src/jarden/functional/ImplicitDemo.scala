package jarden.functional

class MyInt(val num: Int) {
	def +(that: MyInt) = new MyInt(this.num + that.num)
	def +(i: Int) = new MyInt(i + num) // myInt5 + 3
	
	override def toString = s"MyInt($num)"
	override def equals(other: Any) = other match {
		case that: MyInt => that.num == this.num
		case _ => false
	}
	override def hashCode = num.hashCode
}

class Connection(val dbms: String) {
	def apply(sql: String) {
		println(s"send $sql to $dbms") // usually something more interesting that this!
	}
}

object ImplicitDemo extends App {
	// part 1: use implicit to convert Int to MyInt to allow expressions of
	// type Int + MyInt, e.g. 3 + new MyInt(5)
	implicit def int2MyInt(i: Int) = new MyInt(i)
	val myInt3 = new MyInt(3)
	val myInt5 = new MyInt(5)
	val myInt8 = new MyInt(8)
	assert(myInt3 + myInt5 == myInt8)
	assert(myInt5 + 3 == myInt8)
	assert(3 + myInt5 == myInt8)
	
	println("it all seems to add up")

	// part 2: use implicit to supply default value for 2nd parameter
	// to curried function
	
	// usually imported with the dbms stuff:
	implicit val db2Name = new Connection("db2")
	def doSQL(sql: String)(implicit connection: Connection) {
		connection(sql)
	}
	
	doSQL("select * from prices")
	doSQL("select * from prices")(new Connection("mySql"))
	
	

}
