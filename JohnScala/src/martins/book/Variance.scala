package martins.book

class Publication(val title: String) {
	override def toString = getClass().getSimpleName + ": " + title
}
class Book(title: String) extends Publication(title)

object Library {
	val books: Set[Book] =
		Set(
			new Book("Programming in Scala"),
			new Book("Walden"),
			new Book("The way life works"))
	def printBookList(info: Book => AnyRef) {
		for (book <- books) println(info(book))
	}
}

object Customer {
	def getTitle(p: Publication): String = p.title
	Library.printBookList(getTitle)
}

// john's bits (as it were)
// use trait doBooks instead of Function1, so we can see
// what happens if we adjust the variance indicators
object Variance extends App {
	trait DoBooks[-Book, +AnyRef] {
		def apply(b:Book):AnyRef
	}
	def processBooks(f:DoBooks[Book, AnyRef]) = {
		for (book <- Library.books) println(f(book))
	}
	// several implementations of doBooks
	def getTitle = new DoBooks[Book, String] {
		def apply(b:Book) = b.title
	}
	def getString = new DoBooks[Publication, AnyRef] {
		def apply(b:Publication) = b.toString
	}
	def getIt = new DoBooks[AnyRef, AnyRef] {
		def apply(b:AnyRef) = b.toString
	}
	processBooks(getTitle)
	processBooks(getString)
	processBooks(getIt)
	
	// see if this still works on Set[Publication]
	val pubs: Set[Publication] =
		Set(
			new Book("Programming in Scala"),
			new Book("Walden"),
			new Publication("The way life works"))
	def processBooks2(f:DoBooks[Publication, AnyRef]) = {
		for (pub <- pubs) println(f(pub))
	}
	def getTitle2 = new DoBooks[Publication, String] {
		def apply(b:Publication) = b.title
	}
	processBooks2(getTitle2)
	processBooks2(getString)
	processBooks2(getIt)
  
}


