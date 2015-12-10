package syntax

import java.io.File
import scala.io.Source

/**
 * @author John
 */
object Slides extends App {
	val filesHere = (new File("./src/syntax")).listFiles
	
	def getLines(file: File) = {
		Source.fromFile(file).getLines
	}
	def grep(pattern: String) =
		for { file <- filesHere
			if file.getName.endsWith(".scala")
			line <- getLines(file)
			trimmed = line.trim
			if trimmed.matches(pattern)
		} println(file + ": " + trimmed)
	grep("^def.*")
}