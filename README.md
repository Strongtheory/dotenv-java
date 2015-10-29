dotenv-java
-----------
Pure Java implementation of [dotenv](https://github.com/bkeepers/dotenv).

> **Please note:** This project is brand-spanking-new. We're not even published to Maven/JCenter yet. Give us a few weeks to get our act together. In the meantime, you can use the Gradle command (provided at the very bottom of this page) to build and experiment yourself.

### What is it?
dotenv is a really neat Ruby library that allows you to achieve better parity (configuration-wise) between local and non-local environments. If you subscribe to the [12 Factor App](http://12factor.net) philos, then there are two primary stipulations concerning configs:

* Environment-specific configuration must be loaded from [**environment variables**](http://12factor.net/config); and
* There's no place like [**Parity**](http://12factor.net/dev-prod-parity).

The problem here is that each OS has its own esoteric way of setting env vars. So you might have a team consisting of Windows and Mac devs, but the software is ultimately getting deployed to Ubuntu VMs or EC2 instances. On each of these OSes there are different command-line (or UI) methods for setting env vars. Even across different OS version (such as Mac), the process for setting env vars is radically different. **So how do you build an app that reads all its env-specific properties from env vars, achieves configuration parity regardless of what environment its running on, but allows devs to all be on different machines than prod?**

> The solution is to abstract env vars from the OS locally, but then allow operations to configure your non-local VMs with the proper env vars. Enter dotenv.

*But I have a JVM app and don't want any non-JVM "sidecar" processes running alongside my app.

> Enter dotenv-java

The **big** difference between Ruby's `dotenv` and this project is that in Java there is no way to set environment variables (hence no writes to some `ENV['...']` map) from a running process. Hence you can't just use `dotenv-java` and then make direct calls to `System.getenv(...)`; instead you need to use the `dotenv-java` API (see below).

### Requirements
* JDK8. `dotenv-java` is compiled using Java 8 and *does* make use of certain JDK7 and JDK8 features.
 * Java 7 already reached EOL earlier this year, so we don't feel terrible about this.

### How to use `dotenv-java`
`dotenv-java` is a Java library like any other:

#### Get the jar
##### Current Maven coordinates:
    <dependency>
        <groupId>io.hotmeatballsoup</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>0.1.0</version>
    </dependency>
    
##### Current Gradle coordinates:
    compile 'io.hotmeatballsoup:dotenv-java:0.1.0'
    
#### Write a `.env` ("dotenv") file
Create a `.env` file in any directory on your machine. Valid `.env` files should consist of 0+ **entries** where each entry is a newline-delimited string of the form:

    <key>=<value>
    
For example:

    database.username=skroob
    database.password=12345
    
Some other validation rules/dotenv behaviors:

* *Anything* to the left of the equals sign ("`=`") is considered to be a *key*
* *Anything* to the right of the equals sign is considered to be a *value*
* Keys must not contain whitespaces and must be ASCII characters or underscores
* Values can be any characters
 * **NOTE:** If you wrap a value in quotes, the value will be read in *with the quotes included*!!!**
* At least *some* value must be provided for a given key (e.g. `someKey=` is illegal)
 * If for some *strange* reason you need a key defined without a value, you can assign it to `null` (e.g. `someKey=null`); when loaded it will resolve to Java's `null` object reference (**not** a String with a value of `"null"`)
   * If for some even *stranger* reason you actually want to load an env var as a String object with a **String value** of `"null"`, you'll have to put the env var in quotes and then escape them in your own code (e.g. `someKey="null"`, which loads as a String with a value of `"null"`, which you then escape yourself)
* 1-line comments are allowed and must be prefixed with hashes ("`#`") or double-forward-slashes ("`//`")
* You can reference keys inside the values of other keys by wrapping them in `${<keynamehere>}`
 * See [the good `.env` file's `database.db_name` usage](src/test/resources/good/.env) as an example of this
 * This is useful to prevent repeating the same values over and over

#### Get a `Dotenv` instance
Once you have your dotenv file defined, it's time to load it from the code.

    // Default constructor.
    // Expects to see a .env file directly under your user home.
    Dotenv dotenv = new DotenvBuilder().build();
    
    // Customize where dotenv-java expects to find the .env file.
    Dotenv dotenv = new DotenvBuilder()
        .dotenvDir("/opt/myapp")
        .build();
        
    // Examples of different DSL methods in action.
    Dotenv dotenv = new DotenvBuild()
        .failOnMissingConfigs()
        .dotenvDir("/home/myuser/.myapp")
        .build();
        
#### Validating the dotenv file
Once you have a `Dotenv` instance, you can choose to directly validate the dotenv file backing it, or you can just start using the `get(...)` API as described below. Validation is automatically done for you the first time you call `get(...)`, but this gives you the option to validate on app startup and take actions if something is wrong with your configs.

    dotenv.validateDotenvFile();
    
#### Use the API
    // Remember, the first time you call get(...), validateDotenvFile() is also called for you.
    String dbHostAndPort = dotenv.get("dbHostAndPort");  // Here 'dbHostAndPort' is the key
    
    // Using src/test/resources/good/.env as an example...
    // Prints out "SELECT 1 FROM my_table".
    System.out.println(dotenv.get("database.validationQuery"));
    
    // Prints out ""jdbc:fizz/buzz//database.example.com:12345/${database.db_name}"" (with quotes).
    System.out.println(dotenv.get("database.connString"));    

That's it. Important points about its behavior:

1. Anytime `Dotenv#get` is invoked, `dotenv-java` first looks for an actual env var (using [`System.getenv(String)`](http://docs.oracle.com/javase/7/docs/api/java/lang/System.html#getenv(java.lang.String))) and returns the value if the env var is non-null.
2. If no env var exists, `Dotenv#get` returns the value found inside the configured `.env` file.
3. If still no value exists inside the configured `.env` file, `dotenv-java` will either: (a) throw a `DotenvException` if `failOnMissingConfigs()` was specified in the builder, or (b) return null.

Beyond that, our [tests](src/test/java) and [javadocs]() should address most concerns about API usage.

#### Logging
`dotenv-java` uses [SLF4J]() for logging, and logs everything at either the `TRACE` or `ERROR` level.

**For security purposes, `dotenv-java` *deliberately* does not log the values of any env vars or
dotenv vars that it finds. It simply reports *where* it finds them, or that it didn't find them at all.**

### Road map
We are open to PRs of any kind, but the idea is to keep this library as simple and lightweight as possible. Long-term goals might be to start adding subprojects for easier integration with other config libraries (cfg4j, etc.).

### What if I need something that isn't supported?
[Contribute!](CONTRIBUTING.md), or file an [issue](https://github.com/hotmeatballsoup/dotenv-java/issues) and label it as an **enhancement**.

### What if something isn't working as expected?
File an [issue](https://github.com/hotmeatballsoup/dotenv-java/issues) and label it as a **bug**.

### Contributing
[Please see our Contributing page.](CONTRIBUTING.md)

### Developing
#### Build locally

    ./gradlew build