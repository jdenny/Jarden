package test

import jarden.maths.Imag;

object DoImagMaths {
    def main(args: Array[String]):Unit = {
        println("Hola John boy");
        var imagA:Imag = new Imag(2, 3);
        imagA.re = 4;
        println(s"myInum=${imagA}")
    }
}