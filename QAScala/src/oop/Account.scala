package oop

class BankAccount(val number: Int, bal: Double) {
	private var balance = bal
	if (number < 1) throw new AccountException(number + " is invalid")
	if (bal < 0) {
		throw new AccountException("opening balance - " + bal + " - is -ve")
	}
	def this(number: Int) {
		this(number, 0.0)
	}
	def validateDeposit(amount: Double) {
		validateAmount(amount)
	}
	def validateAmount(amount: Double) {
		if (amount < 0) throw new AccountException(amount + " is -ve")
	}
	def deposit(amount: Double) = {
		validateDeposit(amount)
		balance += amount
		balance
	}
	def validateWithdraw(amount: Double) {
		validateAmount(amount)
		if (balance - amount < 0) {
			throw new AccountException(amount + " is more than balance")
		}
	}
	def withdraw(amount: Double) = {
		validateWithdraw(amount)
		balance -= amount
		balance
	}
	def getBalance = balance
}

class SavingsAccount(number: Int, bal: Double, val interestRate: Double)
		extends BankAccount(number, bal) {
	def applyInterest() = deposit(getBalance * interestRate)
}

class DepositAccount(number: Int, bal: Double, overdraft: Double)
		extends BankAccount(number, bal) {
	private var overdraftLimit = overdraft
	
	override def validateWithdraw(amount: Double) {
		validateAmount(amount)
		if (getBalance + overdraftLimit - amount < 0) {
			throw new AccountException(s"Insufficient funds to withdraw $amount")
		}
	}
}

object Main extends App {
	try {
		BankAccount(0)
		println("Error 1: no exception thrown")
	} catch {
		case ae: AccountException => println("exception 1 thrown ok")
	}
	try {
		BankAccount(24, -1)
		println("Error 2: no exception thrown")
	} catch {
		case ae: AccountException => println("exception 2 thrown ok")
	}
	val acc1 = BankAccount(24, 100)
	assert(acc1.deposit(12.34) == 112.34)
	assert(acc1.getBalance == 112.34)
	val depositAcc = new DepositAccount(25, 100, 150)
	try {
		depositAcc.withdraw(251)
		println("Error 3: no exception thrown")
	} catch {
		case ae: AccountException => println("exception 3 thrown ok")
	}
	assert(depositAcc.withdraw(240) == -140)
	val savingsAcc = new SavingsAccount(26, 1000, 0.05)
	val newBal = savingsAcc.applyInterest()
	println("savingsAcc.newBal=" + newBal)
	assert(newBal == 1050)
	
	println("hasta luego")
}

object BankAccount {
	val logger = new ConsoleLogger(Warning)
	def apply(number: Int, bal: Double = 0) = {
		logger.log(s"BankAccount($number, $bal)", Debug)
		new BankAccount(number, bal)
	}
}

class AccountException(message: String = "") extends Exception {
}