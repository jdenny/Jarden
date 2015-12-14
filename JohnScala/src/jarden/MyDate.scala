package jarden

class MyDate (val d:Int, val m:Int, y2:Int) extends MyOrd {
	val y:Int = if (y2 < 20) y2 + 2000 else if (y2 < 100) y2 + 1900 else y2
	
	println(s"y2=$y2; y=$y")
	
    override def equals (other: Any): Boolean = other match {
		case that:MyDate => that.d == this.d && that.m == this.m &&
			that.y == this.y
		case _ => false
	}
	override def hashCode = y * 365 + m * 31 + d
    override def toString(): String = s"$d/$m/$y"
    
    def < (other: Any): Boolean = other match {
    	case that: MyDate => this.y < that.y ||
    		(this.y == that.y && this.m < that.m) ||
    		(this.y == that.y && this.m == that.m && this.d < that.d)
    	case _ => false
    }
}

trait MyOrd {
    def < (that: Any): Boolean
    def <=(that: Any): Boolean = (this < that) || (this == that)
    def > (that: Any): Boolean = !(this <= that)
    def >=(that: Any): Boolean = !(this < that)
}

object MyMain extends App {
	val now = new MyDate(3, 9, 15)
	val tomorrow:Any = new MyDate(4, 9, 2015)
	val lastYear:MyDate = new MyDate(3, 9, 2014)
	assert(now < tomorrow)
	assert(lastYear < now)
	assert(now == new MyDate(3, 9, 2015))
}

