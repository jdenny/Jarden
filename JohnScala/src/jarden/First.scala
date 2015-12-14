package jarden

class A(var name:String = "unknown") {
    def this() = this("John")
    override def toString(): String = s"A.name=${this.name}"
    final def setName(name:String) {
        this.name = name;
    }
    def hello() = println(name)
}

class B(name:String, var num:Int) extends A(name) {
    var salary:Double = 0.0;
    lazy val company:String = {
        println(s"evaluating company for B.name=$this.name")
        "ICL"
    }
    override def toString(): String = super.toString + "; salary=" + this.salary
    // override def setName(name:String) = println("hello!") // can't override as final
}

class Person (var name:String, var salary:Double) {
    println(s"Person created with name=$name")
    var number = Person.getNextNum();
    
    def this() = this("unknown", 0)
    def apply () = println(s"Apply; Person=$this");
    def apply (factor:Double) = salary *= factor;

    
    override def toString(): String =
        s"Name=${this.name}; number=${this.number}; salary=${this.salary}"
}

object Person {
    var nextNum:Int = 0;
    def getNextNum():Int = { nextNum += 1; nextNum }
}

object Me {
    def main(args: Array[String]):Unit = {
        println("Hola John boy");
        useFunctions()
        val doAll:Boolean = false;
        if (doAll) {
	        println(s"args.length=${args.length}")
	        // val days: Array[Int] = new Int[12];
	        var b1:B = new B("julie", 45)
	        b1.setName("Julie Dawn")
	        println(s"b1: $b1")
	        b1 setName "Julie Night"
	        println(s"b1: $b1")
	        println(s"b1.name=${b1.name}; b1.num=${b1.num}")
	        println(s"julie's company=${b1.company}")
	        b1.salary = 33.44;
	        println(s"julie's salary=${b1.salary}")
	        println(s"b1: $b1")
	        val a = new A();
	        a.hello()
	        println(s"a=$a")
	        for (x <- -40 to 40 by 10) {
	        	println(s"cToFx(${x})=${cToF(x)}")
	        }
	        val john:Person = new Person("john", 33.22);
	        println(s"john=$john");
	        john();
	        john(1.5);
	        println(s"john=$john");
	        val julie:Person = new Person("julie", 44.99);
	        println(s"julie=$julie");
	        var a1:A = new A("john")
	        println(s"a1: $a1")
	        a1.setName("Jonathan");
	        println(s"a1: $a1")
	        a1 setName "Jonny";
	        println(s"a1: $a1")
	        var s = a1.toString()
	        println(s"s=$s")
	        s = a1 toString;
	        println(s"s=$s")
	    	var countries = Map("England" -> "London", "India" -> "Delhi", "Scotland" -> "Edinburgh")
	    	countries += ("Spain" -> "Madrid")
	    	println("capital of Spain is " + countries("Spain"))
	    	countries.exists(_ == ("England", "London"))
	    	val name = "John"
	    	// nameHasUpperCase:Boolean = 
        }
        /*
        val john:Person = new Person("John", 32, 12.34)
        println(s"john.salary=${john.salary}")
        printThem("John", "Julie", "Jackie")
        val i:Int = 4;
        val j:Int = -5
        val biggest:Int = {
            if (i>j) i
            else j
        }
        println(s"i=$i, j=$j, biggest=$biggest")
        // val children:String[] = new String[4]
        // {"Sam", "Joe", "Angela", "Sarai"}
        val days = new ArrayList()
        john.salary = 22.44
        println(s"john.salary=${john.salary}")
        */
    }
    def printThem(names: String*):Unit = {
        println("in printThem")
        for (name <- names) {
            println(name)
        }
    }
    def cToF(x:Int) = (x * 9 / 5) + 32
    
    def doSums(f:(String, Int) => String) = f("Buenos Dias", 42)
    def goodbye(s:String, i:Int) = s.toUpperCase() + "=" + i
    def useFunctions() {
    	println(doSums(goodbye)) // BUENOS DIAS=42
    	println(doSums((s:String, i:Int) => i + s.toLowerCase())) // 42buenos dias
    }
}

