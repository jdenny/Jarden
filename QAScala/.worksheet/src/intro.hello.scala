package intro

object hello {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(75); 
	val welcome = "Welcome to hello Scala world";System.out.println("""welcome  : String = """ + $show(welcome ));$skip(19); 
  println(welcome);$skip(26); val res$0 = 
  welcome.groupBy(c => c);System.out.println("""res0: scala.collection.immutable.Map[Char,String] = """ + $show(res$0))}
}
