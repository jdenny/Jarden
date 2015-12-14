package jarden.concurrency

import java.util.concurrent.ConcurrentHashMap

/**
 * Thin wrapper around ConcurrentHashMap, to provide extra method
 * take(key) which blocks until the object for that key is
 * available.
 * @author john.denny@gmail.com
 *
 * @param <K> key for underlying hashmap
 * @param <V> value for underlying hashmap
 */
class JDBlockingMap[K, V] {
	val map = new ConcurrentHashMap[K, V]()
	
	def put(key: K, value: V) {
		map.put(key, value)
		map.synchronized {
			map.notifyAll()
		}
	}
	/**
	 * Get value for this key, or null if no matching key.
	 */
	def get(key: K) = map.get(key)
	/**
	 * Get value for this key; will block if no matching key,
	 * until one is available.
	 * @throws InterruptedException if interrupted while waiting.
	 */
	def take(key: K) = {
		var value: V = map.get(key)
		if (value == null) {
			map.synchronized {
				while (value == null) {
					map.wait();
					value = map.get(key)
				} 
			}
		}
		value
	}
}

class Person(val name: String, val email: String) {
	override def hashCode() = email.hashCode()
	override def equals(other: Any) = other match {
		case that: Person => this.email == that.email
		case _ => false
	}
	override def toString() = "Person(" + name + "; email=" + email + ")"
}

object BlockingMap extends App {
	println("hola mis amigos")
	val blockingMap = new JDBlockingMap[String, Person]();
	val people = List(new Person("john", "john@home.com"),
			new Person("julie", "julie@garden.co.uk"),
			new Person("angela", "angela@france.fr"))
	val r: Runnable = new Runnable {
		def run = println("hello")
	}
	val t = new Thread {
		override def run {
			for (person <- people) {
				Thread.sleep(500);
				println("thread " +
						Thread.currentThread().getName() +
						" about to put Person " + person.name);
				blockingMap.put(person.name, person);
			}
		}
	}
	t.start();
	print("blockingMap.take(julie)="); println(blockingMap.take("julie"));
	print("blockingMap.take(john)="); println(blockingMap.take("john"));
	print("blockingMap.take(angela)="); println(blockingMap.take("angela"));
	
	val tFail = new Thread {
		override def run {
			try {
				blockingMap.take("samNJoe");
				println("samNJoe found!");
			} catch {
				case e: InterruptedException => println("thread " +
					Thread.currentThread().getName() +
					" interrupted waiting for samNJoe")
			}
		}
	}
	tFail.start();
	Thread.sleep(1000);
	println("thread " +
			Thread.currentThread().getName() +
			" about to interrupt thread waiting for samNJoe");
	tFail.interrupt();
	println("Eso es todo, amigos");
}