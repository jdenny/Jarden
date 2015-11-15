package jarden.maths;

public interface MathsIF {
	public long factorial(int n);
	/**
	 * Same as getPrimes(start, count, 0).
	 */
	public long[] getPrimes(long start, int count);
	/**
	 * Get the next count primes > start. If delay > 0, sleep for delay milliseconds
	 * between each calculation.
	 */
	public long[] getPrimes(long start, int count, int delay, boolean resetCache,
		boolean verbose);
	/**
	 * Stop current calculations.
	 */
	public void stop();
}
