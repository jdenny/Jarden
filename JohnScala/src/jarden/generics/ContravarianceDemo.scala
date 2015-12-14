package jarden.generics

import scala.collection.mutable.ListBuffer

/**
 * @author john.denny@gmail.com
 */
object ContravarianceDemo extends App {
	trait ServiceType[-T<:Car] {
		def service(car: T): Unit
	}
	class ServiceRecord(val mileage: Int, val serviceDesc: String) {
		override def toString = "ServiceRecord(" + mileage + ", " +
			serviceDesc + ")"
	}
	
	class Car(val regNum: String) {
		val serviceHistory = ListBuffer[ServiceRecord]()
		def updateServiceHistory(serviceRecord: ServiceRecord) {
			serviceHistory += serviceRecord
		}
		override def toString = getClass.getSimpleName + ": " +
			regNum + " " + (for(sh <- serviceHistory) yield
					("\n  " + sh.toString()))
	}
	class Ford(regNum: String) extends Car(regNum)
	class FordEscort(regNum: String) extends Ford(regNum)
	
	val car = new Car("car1")
	val ford = new Ford("ford2")
	val fordEscort = new FordEscort("fordEscort3")
	// define some concrete versions of ServiceTypes:
	val oilChange = new ServiceType[Car] {
		def service(car: Car) = car.updateServiceHistory(
			new ServiceRecord(15000, "standard oil change"))
	}
	val fordOilFilterChange = new ServiceType[Ford] {
		def service(car: Ford) = car.updateServiceHistory(
			new ServiceRecord(20000, "ford oil filter change"))
	}
	val fordEscortCamBeltChange = new ServiceType[FordEscort] {
		def service(car: FordEscort) = car.updateServiceHistory(
			new ServiceRecord(22000, "ford escort cam belt change"))
	}
	val fordService: ServiceType[Ford] = oilChange // needs contravariance!
	fordService.service(ford); println(ford)
	oilChange.service(fordEscort) // or fordService.service(fordEscort)
	// note: as oilChange is a service type that applies to all cars,
	// it certainly applies to a Ford or a Ford Escort.
	// ServiceType is contravariant; it is doing things to the object
	// passed, so the object passed is a consumer, not
	// a supplier
	fordEscortCamBeltChange.service(fordEscort); println(fordEscort)
	println("adios mis amigitos")

	
	

	// for future use:
	class Garage[T<:Car] {
		val serviceTypes = ListBuffer[ServiceType[T]]()
		def addServiceType(serviceType: ServiceType[T]) {
			serviceTypes += serviceType
		}
		def serviceCar(car: T, f: ServiceType[T]) = f.service(car)
	}
}

