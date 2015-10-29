/**
 * 
 */
package io.hotmeatballsoup.dotenvjava;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author zharvey
 *
 */
public class DotenvBuilderTest {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	// TODO: Remove after tests are implemented.
	public static void main(String[] args) {
		try {
			Dotenv dotenv = new DotenvBuilder()
					.failOnBadDotenv()
					.dotenvDir("/home/yourusernamehere/path/to/dotenv")
					.failOnMissingConfigs()
					.build();
			
			System.out.println("Value of some env var: " + dotenv.get("SOME_ENV_VAR"));
		} catch (DotenvException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void full_dsl_throws_no_exceptions_on_build() {
		assertTrue(true);
	}

	@Test
	public void validate_throws_exception_if_dotenv_does_not_exist() {
		assertTrue(true);
	}

	@Test
	public void validate_throws_exception_if_dotenv_does_not_contain_valid_keyvalue_pairs() {
		assertTrue(true);
	}

	@Test
	public void get_reads_env_var_before_looking_in_dotenv() {
		assertTrue(true);
	}

	@Test
	public void if_env_var_not_defined_then_get_looks_in_dot_env() {
		assertTrue(true);
	}

	@Test
	public void if_fail_on_missing_configs_is_set_then_get_throws_exception_if_not_defined() {
		assertTrue(true);
	}

	@Test
	public void values_with_quotes_get_loaded_with_quotes() {
		assertTrue(true);
	}

	@Test
	public void values_with_null_get_loaded_as_null_ref() {
		assertTrue(true);
	}

	@Test
	public void has_comments_are_ignored() {
		assertTrue(true);
	}

	@Test
	public void double_slash_comments_are_ignored() {
		assertTrue(true);
	}
	
	@Test
	public void multiple_keys_can_be_referenced_and_resolved_inside_values() {
		assertTrue(true);
	}
}
