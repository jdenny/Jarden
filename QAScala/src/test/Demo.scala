package test

object Demo extends App {
	new TestIt
}

class Word {
	def my(word: Word) = {
		println("my()")
		this
	}
	def anything(word: Word) = {
		println("anything()")
		this
	}
}
class DSL {
	val in = new Word
	val goes = new Word
	val Scala = new Word
}

class TestIt extends DSL {
	in.my(Scala).anything(goes)
	in my Scala anything goes
}
