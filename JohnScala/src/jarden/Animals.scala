package jarden

/**
 * @author john.denny@gmail.com
 */

class Animal(val name:String) {
	override def toString = getClass.getSimpleName + ": " + name
	override def equals(other: Any) = other match {
		case that: Animal => that.getClass == this.getClass && that.name == name
		case _ => false
	}
	override def hashCode = name.hashCode()
}
  class Fish(name:String) extends Animal(name)
  class Bird(name:String) extends Animal(name)
  class Mammal(name:String) extends Animal(name)
	class Human(name:String) extends Mammal(name)
	class Dog(name:String) extends Mammal(name)
	class Whale(name:String) extends Mammal(name)

object testAnimal extends App {
	val johnMan = new Human("John")
	val johnDog = new Dog("John")
	val johnMan2 = new Human(johnDog.name)
	assert(johnMan == johnMan2)
	println("todo parece estar bien")
}