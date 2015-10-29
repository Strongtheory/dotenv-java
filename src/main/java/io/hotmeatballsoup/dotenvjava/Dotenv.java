package io.hotmeatballsoup.dotenvjava;

/**
 * Simple API for accessing environment variables locally and non-locally.
 */
public interface Dotenv {
	/**
	 * Load an environment variable, either from a local <code>.env</code> file or
	 * from the actual environment.
	 * 
	 * @param envVar	The name of the env var
	 * @return			Either the value of the env var or <code>null</code> (see above)
	 * @throws			When env vars can't be loaded for some reason
	 */
	String get(String envVar) throws DotenvException;
	
	/**
	 * Validates the dotenv file, checking to make sure that it exists and that all its
	 * key-value pair entries are valid.
	 * 
	 * <p>Proper format of <i>each</i> newline-separated dotenv file entry are as follows:
	 * 
	 * <pre><code>key=value</code></pre>
	 * 
	 * <p>Example <code>.env</code> file:
	 * 
	 * <pre><code>
	 * database.username=skrewb
	 * database.password=12345
	 * data.service.baseURI=https://mydataservice.example.com</code></pre>
	 * 
	 * @throws	DotenvException if the dotenv file or its contents are invalid
	 */
	void validateDotenvFile() throws DotenvException;
}
