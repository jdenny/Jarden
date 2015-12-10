package oop

import java.util.Date

trait Logger {
	def log(message: String, level:LogLevel)
}

sealed abstract class LogLevel(val index: Int) extends Ordered[LogLevel] {
	def compare(that:LogLevel) = this.index - that.index
}

case object Debug extends LogLevel(1)
case object Warning extends LogLevel(2)
case object Error extends LogLevel(3)
case object NoLog extends LogLevel(4)

class ConsoleLogger(val loggerLevel: LogLevel) extends Logger {
	override def log(message: String, level: LogLevel = NoLog) {
		if (level >= loggerLevel) println(new Date() + " " + message)
	}
}