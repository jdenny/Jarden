package jarden

object Timer {
    def myTimer(func: () => Int) = {
        for (a:Int <- 1 to 3) {
            Thread.sleep(100);
            println("func=" + func());
        }
    }
    def doGreeting(func: () => Unit):String = {
    	func()
    	"Done It!"
    }
    def doString(func: (String) => Unit, a:String) = func(a)
    def sayHello(s:String):Unit = println("Hello " + s)
    
    def mySums(func: (Int, Int) => Int, a:Int, b:Int) = {
    	val res:Int = func(a, b)
    	println(s"res=$res")
    }
    // functionList:Array(Int, Int)
    def add(a:Int, b:Int):Int = a + b
    def subtract(a:Int, b:Int):Int = a - b

    def main(args: Array[String]): Unit = {
    	doGreeting(() => println("What is going on?"))
    	doString(sayHello, "John")
    	doString((s:String) => println("Hello " + s), "Julie")
    	doString((s:String) => {
    		println("goodbye" + s)
    	}, "Sam")
        myTimer(() => {
            println("hello")
            42
        })
        mySums(add, 5, 4)
        mySums(subtract, 5, 4)
        mySums((a:Int, b:Int) => a * b, 4, 5)
    }

}