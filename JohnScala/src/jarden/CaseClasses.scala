package jarden

object CaseClasses {

    def main(args: Array[String]): Unit = {
        val c420 = Cash(4, 20)
        println(s"c420=${c420}; hashCode=${c420.hashCode()}")
        val c420b = new Cash(4, 20)
        println(s"c420b=${c420b}; hashCode=${c420b.hashCode()}")
        println(s"c420==c420b: ${c420==c420b}")
        val c421 = new Cash(4, 21)
        println(s"c420==c421: ${c420==c421}")
        println(s"c420.pounds=${c420.pounds}; c420.pence=${c420.pence}")
        val c432 = c420.copy(pence = 32)
        println(s"c432=${c432}")
        
        println("============Now for my version: Money===========")
        val m420 = Money(4, 20)
        println(s"m420=${m420}; hashCode=${m420.hashCode()}")
        val m420b = new Money(4, 20)
        println(s"m420b=${m420b}; hashCode=${m420b.hashCode()}")
        println(s"m420==m420b: ${m420==m420b}")
        val m421 = new Money(4, 21)
        println(s"m420==m421: ${m420==m421}")
        println(s"m420.pounds=${m420.pounds}; m420.pence=${m420.pence}")
        val m432 = m420.copy(pence = 32)
        println(s"m432=${m432}")
        
        println("============Show case classes in action===========")
        println("let's see if we can represent x * 3 + 5 as tokens")
        val varX = Var("x")
        val const3 = Const(3)
        val const5 = Const(5)
        val prod = Product(varX, const3)
        println(s"prod: ${prod}");
        val sum = Sum(prod, const5)
        println(s"sum: ${sum}");
        println("x * 3 + 5:")
        for (x <- 1 to 10) {
	        val env: Environment = { case "x" => x }
	        println(s"  x=$x  ${eval(sum, env)}")
        }
        println("=============Now for 2 variables: x * 3 + y=======")
        val varY = Var("y")
        // val const5 = Const(5)
        println(s"prod: ${prod}");
        val sum2 = Sum(prod, varY)
        println(s"sum2: ${sum2}");
        for (x <- 1 to 3; y <- 1 to 5) {
	        val env: Environment = {
	        	case "x" => x
	        	case "y" => y
	        }
	        println(s"  x=$x; y=$y  ${eval(sum2, env)}")
        }
	}
    type Environment = String => Int
    
	def eval(t: Tree, env: Environment): Int = t match {
        case Sum(l, r) => eval(l, env) + eval(r, env)
        case Diff(l, r) => eval(l, env) - eval(r, env)
        case Product(l, r) => eval(l, env) * eval(r, env)
        case Var(n) => env(n)
        case Const(v) => v
    }
}

case class Cash(pounds: Int, pence: Int)

/*
Equivalent to case class Cash above
 */
class Money(val pounds: Int, val pence: Int) {
    override def toString():String = return "Cash(" + pounds + "," +
    		pence + ")"
    override def equals(other:Any):Boolean = other match {
    	case that:Money => this.pounds == that.pounds && this.pence == that.pence
    	case _ => false
    }
    def copy(pounds:Int = this.pounds, pence:Int = this.pence) =
    	new Money(pounds, pence)
    override def hashCode():Int = pounds * 100 + pence
}

object Money {
    def apply(pounds:Int, pence:Int) = new Money(pounds, pence)
}

abstract class Tree
case class Sum(left:Tree, right:Tree) extends Tree
case class Diff(left:Tree, right:Tree) extends Tree
case class Product(left:Tree, right:Tree) extends Tree
case class Var(name: String) extends Tree
case class Const(value: Int) extends Tree
