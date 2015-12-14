package jarden

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Scanner
import scala.io.Source

object MyFiles {

	def main(args: Array[String]): Unit = {
		println("supply file name (default to /Temp/john.txt): ")
		val scanner = new Scanner(System.in)
		var line = scanner.nextLine()
		scanner.close()
		if (line.length() == 0) line = "/Temp/john.txt"
		val file = new File(line)
		if (file.exists() && file.canRead()) {
			val reader = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file)))
			var fLine:String = ""
			do {
				fLine = reader.readLine()
				if (fLine != null) println(fLine)
			} while (fLine != null);
			reader.close()
			println("Now the Scala approach**************")
			for (s:String <- Source.fromFile(file).getLines()) println(s)
		}
	}

}