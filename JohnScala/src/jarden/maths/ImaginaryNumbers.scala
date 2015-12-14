package jarden.maths

object ImaginaryNumbers {
    def main(args: Array[String]):Unit = {
        println("Hola John boy");
        val inum = Imag() // same as: = Imag.apply()
        // val inum = Imag.apply()
        println(s"inum=${inum}")
        val inumA = new Imag(2, 3);
        val inumB = new Imag(3, 1);
        println(s"inumA.r=${inumA.re}");
        var myInum = inumA + inumB;
        println(s"myInum=${myInum}")
        myInum = inumA - inumB;
        println(s"myInum=${myInum}")
        myInum = inumA * inumB;
        println(s"myInum=${myInum}")
        val s = myInum.str
        println(s)
        println(inumA + 5)
        val in = 4 +: inumA
        println("4 +: inumA = " + in)
        println("Adios mi amigito")
    }
}

class Imag(var re:Int, var im:Int) {
    def +(that:Imag):Imag = {
        new Imag(this.re + that.re, this.im + that.im)
    }
    def +(re:Int) = new Imag(re + this.re, this.im)
    def +:(re:Int) = new Imag(re + this.re, this.im)
    def -(that:Imag):Imag = {
        new Imag(this.re - that.re, this.im - that.im)
    }
    def *(that:Imag):Imag = {
        new Imag(this.re * that.re - this.im * that.im,
                this.re * that.im + this.im * that.re)
    }
    override def toString():String = s"(${this.re}, ${this.im}i)";
    def str = toString()
}

object Imag {
    def apply() = new Imag(1, 1)
}

