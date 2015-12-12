package intro

object hello {
	val welcome = "Welcome to hello Scala world"
                                                  //> welcome  : String = Welcome to hello Scala world
  println(welcome)                                //> Welcome to hello Scala world
  welcome.groupBy(c => c)
}