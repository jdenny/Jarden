package jarden

import scala.collection.mutable.Map

/*
Summary of constructor options and effect on visibility
(name:String) see Person2
	private final String name;
(val name:String) see Person4
	private final String name;
	public
		n = person.name;
(var name:String) see Person3
	private String name;
	public
		n = person.name;
		person.name = "new value"
(private var name:String) see Person6
	private String name;
	public
		n = person.name;

Summary of the summary:
	variable is always private; final unless 'var'
	Qualifier		access in class		access outside class
	---------		---------------		--------------------	
	-				read				-
	val				read				read
	private var		read/write			-
	var				read/write			read/write
 */

// Java: private final String name;
class Person2 (name:String) {
    override def toString():String = this.name 
    // def mySetName(name: String) = this.name = name // can't reassign to val
}

// Java: private String name; public String name(); public void name_$eq(String)
class Person3 (var name:String) {
    override def toString():String = this.name 
    def mySetName(name: String) = this.name = name
    val now = new java.util.Date().toString()
    def getDate = new java.util.Date().toString()
    var visits = 0
}

// Java: private final String name; public String name()
class Person4 (val name:String) {
    override def toString():String = this.name 
    // def mySetName(name: String) = this.name = name // can't reassign to val
}

// Java: private final String name; private String name()
// no advantage over Person2, so 'private val' is waste of ink
class Person5 (private val name:String) {
    override def toString():String = this.name 
    // def mySetName(name: String) = this.name = name // can't reassign to val
}

// Java: private String name; private String name(); private void name_$eq(String)
// similar to Person2, but class can update name
class Person6 (private var name:String) {
    override def toString():String = this.name 
    def mySetName(name: String) = this.name = name
}

class Person7 {
	private var name:String = "unknown"
	override def toString() = "Person7[" + name + "]"
	def setName(name:String) = this.name = name
}

object Inheritance {

    def main(args: Array[String]): Unit = {
        val john2 = new Person2("John2")
        // john2.name = "Julie2" // not member
        // println(john2.name) // not member
        println(john2)
        val john3 = new Person3("John3")
        john3.name = "Julie3"
        println(john3.name)
        val john4 = new Person4("John4")
        // john4.name = "Julie4" // immutable
        println(john4.name)
        val john5 = new Person5("john5")
        // john5.name = "Julie5" // not accessible
        // println(john5.name) // not accessible
        println(john5)
        val john6 = new Person6("John6")
        // john6.name = "Julie6" // not accessible
        // println(john6.name) // not accessible
        john6.mySetName("Julie6")
        println(john6)
        val per7 = new Person7
        per7.setName("John7")
        per7 setName "John7a"
        println(per7)
        
        println(john3.getDate)
        println(john3.now)
        john3.visits += 1
        println(s"john3.visits=${john3.visits}")
        john3.visits += 1
        println(s"john3.visits=${john3.visits}")
    }
    import scala.collection.mutable.Map
    private val hashcodeMap = Map[String, Int]()
    def getHash(s:String):Int = {
    	val res = hashcodeMap.get(s)
    	if (res == None) {
    		
    	}
    	5
    }
    
}