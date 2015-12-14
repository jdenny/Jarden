package mallon.functional

import java.net.URL
import scala.util.{Try, Success, Failure}
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

object Lab6Exception {

	def main(args: Array[String]): Unit = {
		println("starting Exceptions")
		val result = parseURL("file:/temp/john2.txt") 
		assert(result.isSuccess)
		assert(result.isInstanceOf[Success[URL]])
		assert(result.get.getPath() == "/temp/john2.txt")
		val badResult = parseURL("dollop")
		assert(badResult.isFailure)
		val url = badResult match {
			case Success(url) => url
			case Failure(ex) => {result.get}
		}
		assert(url == result.get)
		assert(inputStreamForURL2("fred").isFailure)
		assert(inputStreamForURL2("file:/fred").isFailure)
		val tryInStream = inputStreamForURL2("file:/temp/john2.txt")
		assert(tryInStream.isSuccess)
		val inStream = tryInStream.get
		val reader = new InputStreamReader(inStream)
		val buffReader = new BufferedReader(reader)
		//? while (line = buffReader.readLine != null) println(line)
		buffReader.close()
		val seq = Seq("file:/temp/john2.txt", "dollop", "file:/fred")
		val multiUrls = seq.map(s => parseURL(s))
		println(multiUrls)
		// multiUrls.flatten
		println("ending Exceptions")
	}
	def parseURL(s:String):Try[URL] = {
		Try (new URL(s)) // or Try { new URL(s)) }
	}
	def inputStreamForURL(url: String): Try[Try[Try[InputStream]]] = {
		parseURL(url).map { u =>
			Try(u.openConnection()).map { conn =>
				Try(conn.getInputStream)
			}
		}
	}
	// same as above, but with flatMap instead of map, which removes the nested
	// Try types. Also shows we can use ( below or { above in some cases.
	def inputStreamForURL2(url: String): Try[InputStream] =
		parseURL(url).flatMap(u =>
			Try(u.openConnection()).flatMap(conn =>
				Try(conn.getInputStream)))
}