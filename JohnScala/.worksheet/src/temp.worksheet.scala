package temp

import java.net.URL
import scala.util.{ Try, Success, Failure }
import java.io.InputStream

object worksheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(210); 
	def parseURL(s: String): Try[URL] = {
		Try(new URL(s)) // or Try { new URL(s)) }
	};System.out.println("""parseURL: (s: String)scala.util.Try[java.net.URL]""");$skip(162); 
	def inputStreamForURL(url: String): Try[Try[Try[InputStream]]] =
		parseURL(url).map { u =>
			Try(u.openConnection()).map(conn => Try(conn.getInputStream))
		};System.out.println("""inputStreamForURL: (url: String)scala.util.Try[scala.util.Try[scala.util.Try[java.io.InputStream]]]""");$skip(161); 
	def inputStreamForURL2(url: String): Try[InputStream] =
		parseURL(url).flatMap { u =>
			Try(u.openConnection()).flatMap(conn => Try(conn.getInputStream))
		};System.out.println("""inputStreamForURL2: (url: String)scala.util.Try[java.io.InputStream]""");$skip(29); val res$0 = 
		inputStreamForURL2("fred");System.out.println("""res0: scala.util.Try[java.io.InputStream] = """ + $show(res$0));$skip(35); val res$1 = 
		inputStreamForURL2("file:/fred");System.out.println("""res1: scala.util.Try[java.io.InputStream] = """ + $show(res$1));$skip(45); val res$2 = 
		inputStreamForURL2("file:/temp/john2.txt");System.out.println("""res2: scala.util.Try[java.io.InputStream] = """ + $show(res$2));$skip(31); 
		println("ending Exceptions")}

}
