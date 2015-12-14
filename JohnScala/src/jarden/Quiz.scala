package jarden

import java.util.Random

class Quiz(var question: String = "",
    var correctAnswer: Int = 0,
    val randomNum: Random = new Random()) {

    def isCorrect(answer: Int): Boolean = answer == this.correctAnswer
    def getCorrectAnswer(): Int = this.correctAnswer
    
    def apply() = getNextQuestion()

    def getNextQuestion(): String = {
        var maxInt = 20
        val opCode = randomNum.nextInt(4)
        // weight the maxInt according to the operator,
        // so the result is a similar magnitude:
        opCode match {
            case 0 => maxInt = (maxInt + 1) / 2 // add
            case 1 => 1 // subtract
            case 2 => maxInt = Math.sqrt(maxInt).intValue() + 1 // multiply
            case 3 => maxInt = Math.sqrt(maxInt).intValue() + 1 // divide
        }
        var a: Int = randomNum.nextInt(maxInt) + 1
        val b: Int = randomNum.nextInt(maxInt) + 1
        this.correctAnswer = a - b
        val op: Char = opCode match {
            case 0 => {
                this.correctAnswer = a + b
                '+'
            }
            case 1 => {
                this.correctAnswer = a - b
                '-'
            }
            case 2 => {
                this.correctAnswer = a * b
                '*'
            }
            case default => {
                // shuffle the values a bit, so that the answer is an integer
                this.correctAnswer = a
                a = a * b
                '/'
            }
        }
        this.question = a + " " + op + " " + b + " = "
        return this.question
    }

}

object John extends App {
    println("Hola John boy")
    val quiz: Quiz = new Quiz()
    var finished:Boolean = false
    while (!finished) {
        // var nextQ = quiz.getNextQuestion()
        val nextQ = quiz()
        println(s"what is ${nextQ}: ")
        val line:String = readLine()
        if (line == null || line.startsWith("q")) {
            finished = true
        } else {
            val answer = Integer.parseInt(line)
            if (quiz.isCorrect(answer)) {
                println("correct!")
            } else {
                val ca = quiz.getCorrectAnswer()
                println(s"Wrong! correct answer is: $ca")
            }
        }
    }
    println("Adios mi amigito")
}
