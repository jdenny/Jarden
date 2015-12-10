package oop

object Traits extends App {
	val rect = new Rectangle(10, 20, 40, 50)
	println("rect=" + rect)
	val rect2 = rect.move(2, 3)
	println(s"rect=${rect}; rect2=${rect2}")
	assert(rect == new Rectangle(10, 20, 40, 50))
	assert(rect2 == new Rectangle(12, 23, 40, 50))
	assert(rect2 != "Shape(12, 23, 40, 50)")
	assert(rect2.toString() == "Shape(12, 23, 40, 50)")
	
	val annotatedShape = new Rectangle(1, 2, 3, 4) with Annotated
	assert(annotatedShape.isInstanceOf[Shape])
	assert(annotatedShape.isInstanceOf[Rectangle])
	assert(annotatedShape.isInstanceOf[Annotated])
	annotatedShape.setText("small rectangle")
	assert(annotatedShape.getText() == "small rectangle")
	val runnableShape = new Rectangle(20, 22, 23, 24) with Runnable {
		override def run() = println("runnable Rectangle=" + this)
	}
	runnableShape.run()
	val goForIt = new GoForIt
	goForIt.go()
	goForIt.go1()
	goForIt.go2()
	goForIt.go3()
	println("adios mis amiguitas")
}

abstract class Shape(val x: Int, val y: Int, val h: Int, val w: Int) {
	override def toString = s"Shape($x, $y, $h, $w)"
	def equals2(other: Any) = 
		if (!other.isInstanceOf[Shape]) false
		else {
			val that = other.asInstanceOf[Shape]
			that.x == this.x && that.y == this.y &&
				that.h == this.h && that.w == this.w
		}
	override def equals(other: Any) = other match {
		case that: Shape => that.x == this.x && that.y == this.y &&
				that.h == this.h && that.w == this.w
		case _ => false
	}
}

trait Annotated {
	var text = "unknown"
	def setText(t: String) { this.text = t }
	def getText() = this.text
}

trait Moveable {
	def move(dx: Int, dy: Int): AnyRef
}

trait Resizeable {
	def resize(dh: Int = 0, dw: Int = 0): AnyRef
}

class Rectangle(x: Int, y: Int, h: Int, w: Int)
		extends Shape(x, y, h, w) with Moveable with Resizeable {
	override def move(dx: Int = 0, dy: Int = 0) =
		new Rectangle(x + dx, y + dy, h, w)
	override def resize(dh: Int = 0, dw: Int = 0) =
		new Rectangle(x, y, h + dh, w + dw)
	
}

trait Go1 {
	def go() = println("trait Go1.go")
	def go1() = println("trait Go1.go1")
}
trait Go2 {
	def go() = println("trait Go2.go")
	def go1() = println("trait Go2.go1")
	def go2() = println("trait Go2.go2")
}
trait Go3 {
	def go() = println("trait Go3.go")
	def go1() = println("trait Go3.go1")
	def go2() = println("trait Go3.go2")
	def go3() = println("trait Go3.go3")
}

class GoForIt extends Go1 with Go3 with Go2 {
	override def go = {
		println("class GoForIt.go")
		super.go()
	}
	override def go1 = {
		println("class GoForIt.go1")
		super.go1()
	}
	override def go2 = {
		println("class GoForIt.go2")
		super.go2()
	}
}