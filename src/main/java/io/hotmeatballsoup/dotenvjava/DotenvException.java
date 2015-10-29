package io.hotmeatballsoup.dotenvjava;

/**
 * Thrown when {@link Dotenv} runs into trouble accesing env vars.
 */
public class DotenvException extends Exception {
	private static final long serialVersionUID = 1L;

	public DotenvException(String message) {
		super(message);
	}
	
	public DotenvException(Exception exc) {
		super(exc);
	}
	
	@Override
	public String toString() {
		return super.getMessage();
	}
}
