package jarden.generics

import scala.reflect.ClassTag
import jarden.{Animal,Mammal,Human,Dog,Whale}

/**
 * @author john.denny@gmail.com
 */
object Variance6 extends App {
	val john = new Mammal("john")
	val julie = new Mammal("julie")
	val sam = new Dog("sam")
	val joe = new Dog("joe")
	val wrapSam = new AnimalWrap(sam)
	val wrapJulie = new AnimalWrap(julie)
	val wrapSamJulie = wrapSam + julie
	println(wrapSamJulie)
	val wSam = new Wrap(sam)
	println(wSam)
	val wJoe = new Wrap(joe)
	println(wSam + wJoe)
	val wJulie = new Wrap(julie)
	println(wJulie + wSam)
	println(wJulie + joe)
}

class AnimalWrap(val animal: Animal) {
	def +(that: Animal) =
		new Animal(animal.name + " " + that.name)
	def +(that: AnimalWrap) =
		new Animal(animal.name + " " + that.animal.name)
}

class Wrap[T <: Animal](val t: T) {
	override def toString =
		"Wrap[" + t.getClass.getSimpleName + "]: " + t.name
	def +[S <: T](that: Wrap[S]) =
		makeCopy(t.name + " " + that.t.name)
	def +[S <: T](that: S) =
		makeCopy(t.name + " " + that.name)
	private def makeCopy(name: String) = t match {
		case h: Human => new Human(name)
		case d: Dog => new Dog(name)
		case m: Mammal => new Mammal(name)
		case _ => new Animal(name)
	}
//	def +(that: AnimalWrap) =
//		new Animal(animal.name + " " + that.animal.name)
}
