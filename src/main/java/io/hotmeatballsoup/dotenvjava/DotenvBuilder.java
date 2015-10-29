package io.hotmeatballsoup.dotenvjava;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and provides a concrete {@link Dotenv} instance.
 */
public class DotenvBuilder {
	private static final String DIR_DELIM = "/";
	
	private String dotenvDir = System.getProperty("user.home");
	
	@SuppressWarnings("unused")
	private boolean failOnBadDotenv = false;
	
	private boolean failOnMissingConfigs = false;
	
	private boolean ignoreBadEntries = false;
		
	/**
	 * Creates a {@link DotenvBuilder} instances.
	 */
	public DotenvBuilder() {
		super();
	}
	
	/**
	 * Specifies that the resultant {@link Dotenv} impl should throw a {@link DotenvException}
	 * if the dotenv file is either not found at the expected location, or consists of malformed
	 * key-value pairs.
	 * 
	 * <p>Proper format for each env var defined inside the dotenv file is:
	 * 
	 * <pre><code>key=value</code></pre>
	 * 
	 * @return	This builder
	 */
	public DotenvBuilder failOnBadDotenv() {
		this.failOnBadDotenv = true;
		
		return this;
	}
	
	/**
	 * Specifies that the resultant {@link Dotenv} impl should throw a {@link DotenvException} if
	 * neither the dotenv file nor the OS (environment) provide values for a particular config.
	 * 
	 * @return	This builder
	 */
	public DotenvBuilder failOnMissingConfigs() {
		this.failOnMissingConfigs = true;
		
		return this;
	}
	
	/**
	 * Specifies that the resultant {@link Dotenv} impl should look for the dotenv file at the
	 * provided directory.
	 * 
	 * @param env	The directory containing the dotenv file
	 * @return		This builder
	 */
	public DotenvBuilder dotenvDir(String dotenvDir) {
		if(!dotenvDir.endsWith(DIR_DELIM)) {
			dotenvDir += DIR_DELIM;
		}
		
		this.dotenvDir = dotenvDir;
		
		return this;
	}
	
	/**
	 * Set this if you don't care about syntax errors inside your dotenv file. This will prompt
	 * the library to simply verify a dotenv file exists, but will not attempt to validate it.
	 * 
	 * @return	This builder
	 */
	public DotenvBuilder ignoreBadEntries() {
		this.ignoreBadEntries = true;
		
		return this;
	}
	
	/**
	 * Builds and returns a fully configured {@link Dotenv} instance.
	 * 
	 * @return		A {@link Dotenv} impl
	 * @throws		A {@link DotenvException} if the dotenv file is invalid or malformed
	 */
	public Dotenv build() throws DotenvException {
		return new DefaultDotenv(failOnMissingConfigs, dotenvDir, ignoreBadEntries);
	}
	
	private class DefaultDotenv implements Dotenv {
		private Logger logger = LoggerFactory.getLogger(DefaultDotenv.class);
		
		private static final String COMMENT_HASH = "#";
		private static final String COMMENT_SLASHES = "//";
		
		private boolean failOnMissingConfigs;
		private String dotenvDir;
		private boolean ignoreBadEntries;
		
		private Map<String,String> dotenvVars;
		
		public DefaultDotenv(boolean failOnMissingConfigs, String dotenvDir, boolean ignoreBadEntries) {
			super();
			
			setFailOnMissingConfigs(failOnMissingConfigs);
			setDotenvDir(dotenvDir);
		}

		@Override
		public void validateDotenvFile() throws DotenvException {
			dotenvVars = new HashMap<String,String>();
			String content;
			
			try {
				System.out.println("Dotenv Dir: " + dotenvDir);
				content = new String(Files.readAllBytes(Paths.get(dotenvDir + DIR_DELIM + ".env")));
				
				// If the user has specified to ignore bad entires, there's no sense in
				// attempting to validate them.
				if(isIgnoreBadEntries()) {
					return;
				}
				
				StringTokenizer dotenvTokenizer = new StringTokenizer(content, "\n");
				while(dotenvTokenizer.hasMoreTokens()) {
					String line = dotenvTokenizer.nextToken();
					
					if(line.startsWith(COMMENT_HASH) || line.startsWith(COMMENT_SLASHES)) {
						continue;
					}
					
					StringTokenizer varTokenizer = new StringTokenizer(line, "=");
					if(varTokenizer.countTokens() != 2) {
						String errorMsg = new StringBuilder()
							.append("Malformed dotenv file. The following entry is invalid: \"")
							.append(line)
							.append("\". All dotenv entries must be of the form: \"key=value\".")
							.toString();
						logger.error(errorMsg);
						throw new DotenvException(errorMsg);
					}
					
					dotenvVars.put(varTokenizer.nextToken(), varTokenizer.nextToken());
				}
			} catch (Exception exc) {
				logger.error(ExceptionUtils.getStackTrace(exc));
				throw new DotenvException(exc);
			}
			
			// TODO: We'll come back to this once we're done implementing it.
//			resolveKeyReferences();
		}
		
		// The 'validateDotenvFile' method pulls all the key-value pairs defined in the dotenv
		// file into the 'dotenvVars' hashmap. However some values in this map might reference
		// other keys, so we need to resolve them here.
		@SuppressWarnings("unused") // TODO: Remove this once implemented & tested.
		private void resolveKeyReferences() throws DotenvException {
			Pattern refPattern = Pattern.compile("\\$\\{[^{}]*}");
			for(String key : dotenvVars.keySet()) {
				String value = dotenvVars.get(key);
				Matcher matcher = refPattern.matcher(value);
				while(matcher.find()) {
					String extractedKeyRef = matcher.group();
					String extractedKeyRefValue = dotenvVars.get(extractedKeyRef);
					if(extractedKeyRefValue == null) {
						throw new DotenvException(
							String.format(new StringBuilder()
								.append("The key \"%s\" references another key (\"%s\") that ")
								.append("does not exist in the dotenv file.")
								.toString(), key, extractedKeyRef));
					}
					String newValue = value; // = injectTemplate(value, extractedKeyRefValue)
					dotenvVars.put(key, newValue);
				}
			}
		}
		
		@Override
		public String get(String envVar) throws DotenvException {
			if(dotenvVars == null) {
				validateDotenvFile();
			}
			
			String value = System.getenv(envVar);
			if(value == null) {
				logger.trace(String.format(new StringBuilder()
					.append("Did not find an environment variable named \"%s\". ")
					.append("Attempting to read from dotenv file.").toString(), envVar));
				value = dotenvVars.get(envVar);
				
				if(value == null) {
					logger.trace(String.format(new StringBuilder()
						.append("Did not find a dotenv variable named \"%s\" defined ")
						.append("inside the dotenv file either.").toString(), envVar));
					if(isFailOnMissingConfigs()) {
						String errorMsg = String.format(
							new StringBuilder()
							.append("\"FailOnMissingConfigs\" is turned on. ")
							.append("Throwing an exception because an environment variable ")
							.append("named \"%s\" could not be found anywhere.").toString(), envVar);
						
						logger.error(errorMsg);
						throw new DotenvException(errorMsg);
					}
				} else {
					logger.trace(String.format(
						"A variable named \"%s\" was found in the dotenv file.",
						envVar));
				}
			} else {
				logger.trace(String.format(
					"An environment variable named \"%s\" was loaded directly from the environment.",
					envVar));
			}
			
			return value;
		}
		
		public boolean isFailOnMissingConfigs() {
			return this.failOnMissingConfigs;
		}
		
		public boolean isIgnoreBadEntries() {
			return this.ignoreBadEntries;
		}

		public void setFailOnMissingConfigs(boolean failOnMissingConfigs) {
			this.failOnMissingConfigs = failOnMissingConfigs;
		}

		@SuppressWarnings("unused")
		public String getDotenvDir() {
			return this.dotenvDir;
		}
		
		public void setDotenvDir(String dotenvDir) {
			this.dotenvDir = dotenvDir;
		}
	}
}
