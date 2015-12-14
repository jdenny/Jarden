package jarden

object OperatorPrecedence {

    def main(args: Array[String]): Unit = {
        val m1 = new MyInt(1)
        val m2 = new MyInt(2)
        val m3 = new MyInt(3)
        val m4 = new MyInt(4)
        
        var m = m1
        m += m1
        println(s"m=$m")
        m += m1 + m2 - m3 * m4 / m2 add m1 * m1 sub m2 - m3 +
        	m4 mult m2 add m3
        println(s"m=$m")
        
        m = m1 add m2 sub m3 mult m4 div m2 mult m1 sub m2 add m1
        println(s"m=$m")
        m = m1 add_: m2 sub_: m3 mult_: m4 div_: m2 mult_: m1 sub_: m2 add_: m1
        println(s"m=$m")
    }

}

class MyInt(var i:Int) {
    override def toString() = i.toString()
    def +(that:MyInt) = {
        println(s"MyInt($i) + MyInt(${that.i})")
        new MyInt(this.i + that.i)
    }
    def -(that:MyInt) = {
        println(s"MyInt($i) - MyInt(${that.i})")
        new MyInt(this.i - that.i)
    }
    def *(that:MyInt) = {
        println(s"MyInt($i) * MyInt(${that.i})")
        new MyInt(this.i * that.i)
    }
    def /(that:MyInt) = {
        println(s"MyInt($i) / MyInt(${that.i})")
        new MyInt(this.i / that.i)
    }
    def +=(that:MyInt) = {
        println(s"MyInt($i) += MyInt(${that.i})")
        this.i += that.i
    }
    def -=(that:MyInt) = {
        println(s"MyInt($i) -= MyInt(${that.i})")
        this.i -= that.i
    }
    def *=(that:MyInt) = {
        println(s"MyInt($i) *= MyInt(${that.i})")
        this.i *= that.i
    }
    def /=(that:MyInt) = {
        println(s"MyInt($i) /= MyInt(${that.i})")
        this.i /= that.i
    }
    def add(that:MyInt) = {
        println(s"MyInt($i) add MyInt(${that.i})")
        new MyInt(this.i + that.i)
    }
    def sub(that:MyInt) = {
        println(s"MyInt($i) sub MyInt(${that.i})")
        new MyInt(this.i - that.i)
    }
    def mult(that:MyInt) = {
        println(s"MyInt($i) mult MyInt(${that.i})")
        new MyInt(this.i * that.i)
    }
    def div(that:MyInt) = {
        println(s"MyInt($i) div MyInt(${that.i})")
        new MyInt(this.i / that.i)
    }
    def add_:(that:MyInt) = {
        println(s"MyInt($i) add_: MyInt(${that.i})")
        new MyInt(this.i + that.i)
    }
    def sub_:(that:MyInt) = {
        println(s"MyInt($i) sub_: MyInt(${that.i})")
        new MyInt(this.i - that.i)
    }
    def mult_:(that:MyInt) = {
        println(s"MyInt($i) mult_: MyInt(${that.i})")
        new MyInt(this.i * that.i)
    }
    def div_:(that:MyInt) = {
        println(s"MyInt($i) div_: MyInt(${that.i})")
        new MyInt(this.i / that.i)
    }
    
}