package mallon.generics;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class CapitalSupplier implements Supplier<String> {
	private String country;
	
	public CapitalSupplier(String country) {
		this.country = country;
	}
	@Override
	public String get() {
		try {
			return Capitals.getCapital(country);
		} catch (CountryNotFoundException e) {
			System.out.println(e);
			return "unknown";
		}
	}
}

public class TestFutures {
	public static void main(String[] args) {
		System.out.println(Thread.currentThread());
		getCapital("England");
		getCapital("Germany");
		getCapital("India");
		System.out.println("adios mis amigos");
	}
	private static String printMessage(String message) {
		System.out.println(message);
		return message;
	}
	private static void getCapital(String country) {
		Supplier<String> supplier = new CapitalSupplier(country);
		CompletableFuture<String> cf = CompletableFuture.supplyAsync(supplier);
		cf.thenCompose(capital -> CompletableFuture.supplyAsync(
				() -> printMessage("the capital(3) of " + country + " is " + capital)));
	}
}

class Capitals {
	private static String[][] dictionary = {
			{"England", "London"},
			{"India", "New Dheli"},
			{"Russia", "Moscow"}
	};
	public static String getCapital(String country) throws CountryNotFoundException {
		System.out.println("getCapital(" + country +
				") " + Thread.currentThread());
		for (int i = 0; i < 6; i++) {
			System.out.print(".");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println(e);
				throw new CountryNotFoundException(country);
			}
		}
		for (String[] entry: dictionary) {
			if (entry[0].equals(country)) return entry[1];
		}
		throw new CountryNotFoundException(country);
	}
}

class CountryNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public CountryNotFoundException() {
		super();
	}
	public CountryNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public CountryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	public CountryNotFoundException(String message) {
		super(message);
	}
	public CountryNotFoundException(Throwable cause) {
		super(cause);
	}
}
