package temp

object Testworksheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(60); 
  println("hello John");$skip(40); 
  val f:(Int) => Int = (a:Int) => a * a;System.out.println("""f  : Int => Int = """ + $show(f ));$skip(17); 
  val res = f(3);System.out.println("""res  : Int = """ + $show(res ))}
}
