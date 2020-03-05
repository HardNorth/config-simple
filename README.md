# A simple configuration library
[![Download](https://jitpack.io/v/HardNorth/config-simple.svg)](https://jitpack.io/#HardNorth/config-simple)
[![Build Status](https://travis-ci.com/HardNorth/config-simple.svg?branch=master)](https://travis-ci.com/HardNorth/config-simple)
[![License](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](https://opensource.org/licenses/Apache-2.0)

## Table of Contents
**[Overview](#overview)**<br/>
**[Getting started](#getting-started)**<br/>
**[Usage](#usage)**<br/>
**[Motivation](#motivation)**<br/>

## Overview
The library is designed to provide a basic config-free *.property*-file based configuration management 
functionality suitable to use with any JVM-based language.
 
It supports:
* File-based environment properties, by specifying property `env=environment_name`.
* Properties override in such a chain: *default properties &larr; file properties &larr; environment variables
  &larr; system properties*. <br/>
  That means:
  * User provides default properties through code
  * Which are overrided by file-based environment properties
  * Which are overrided by the same named environment variables
  * Which are overrided by JVM system properties
* Placeholder resolve functionality in a Spring-framework fashion, like: `my.property.value=${MY_VALUE:default value}`.
Including recursive placeholders.
* Default property values.

## Getting started
### Gradle
**Step 1:** Add a repository into your basic *build.gradle* file:
```groovy
repositories {
	...
	maven { url 'https://jitpack.io' }
}
``` 
Or if you are using sub-projects:
```groovy
allprojects {
    repositories {
	    ...
	    maven { url 'https://jitpack.io' }
    }
}
``` 
**Step 2:** Add dependency:
```groovy
dependencies {
    implementation 'com.github.HardNorth:config-simple:1.0.0'
}
```
### Maven
**Step 1:** Specify a repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
**Step 2:** Add a dependency:
```xml
<dependency>
    <groupId>com.github.HardNorth</groupId>
    <artifactId>config-simple</artifactId>
    <version>1.0.0</version>
</dependency>
``` 
### SBT
**Step 1:** Add it in your *build.sbt* at the end of resolvers:
```sbt
resolvers += "jitpack" at "https://jitpack.io"
```
**Step 2:** Add a dependency:
```sbt
libraryDependencies += "com.github.HardNorth" % "config-simple" % "1.0.0"
```
## Usage
### Basic case
By standard behavior (if there is no specified `env` parameter somewhere in environment and system properties)
the library seeks for `default.properties` file in classpath and reads it. So let's place such file,
for example into `src/main/resources`.
```properties
test.string.value=my string value 1
```
Then let's implement a class which will read the file:
```java
import com.github.hardnorth.common.config.ConfigLoader;
import com.github.hardnorth.common.config.ConfigProvider;

public class Config {
    private static final ConfigProvider PROVIDER = new ConfigLoader().get();

    public static final String STRING_VALUE = PROVIDER.getProperty("test.placeholder.value", String.class);

    // just for testing and examples, remove from real code
    public static void main(String[] args) {
        // Will output "my string value 1"
        System.out.println(STRING_VALUE);
    }
}
```
Now we can use our property anywhere in an application referring it as a constant:
```java
LOGGER.info(Config.STRING_VALUE); // Will log "my string value 1"
```
### Type conversion
The library also supports more complex type conversion:
```properties
test.url.value=https://www.example.com
```
Such value will be set correctly:
```java
public static final URL URL_VALUE = PROVIDER.getProperty("test.url.value", URL.class);
``` 
### In-code default values
There is also possible to set a default value on the fly:
```java
// Will be "my default value"
public static final String NOT_EXISTING_VALUE = PROVIDER.getProperty("test.not.existing.value", String.class, "my default value");
```
### Environments
The library provides environment switch. To switch on different property file use `env` parameter specified
somewhere in environment or system properties, or bypassed as a default property to `ConfigLoader` constructor. 
To demonstrate the feature let's update `default.properties` with additional value:
```properties
test.url.value=https://www.example.com
```
And add another property file, let's say `dev.properties` with:
```properties
test.url.value=http://localhost
```
Our new Config class:
```java
import com.github.hardnorth.common.config.ConfigLoader;
import com.github.hardnorth.common.config.ConfigProvider;

import java.net.URL;

public class Config {
    private static final ConfigProvider PROVIDER = new ConfigLoader().get();

    public static final URL URL_VALUE = PROVIDER.getProperty("test.url.value", URL.class);

    // just for testing and examples, remove from real code
    public static void main(String[] args) {
        // Will output an URL
        System.out.println(URL_VALUE);
    }
}
```
Now we can run our class like this:
```bash
#!/bin/bash

export env=dev
java -cp config-simple-1.0.1-SNAPSHOT-all.jar:. Config
```
This code outputs: `http://localhost`.
It also possible to bypass `env` param through System properties:
```bash
java -cp config-simple-1.0.1-SNAPSHOT-all.jar:. -Denv=dev Config
``` 
Will output: `http://localhost`.
But:
```bash
#!/bin/bash

export env=dev
java -cp config-simple-1.0.1-SNAPSHOT-all.jar:. -Denv=default Config
```
Will output: `https://www.example.com`, since System properties have greater weight in the library than
Environment variables. As was specified the inheritance/override chain looks like this (from lower weight to greater weight):
`default properties <- file properties <- environment variables <- system properties`

### Basic placeholders
Let's update our `default.properties` file:
```properties
test.string.value=my string value 1
test.placeholder.value=${test.string.value}
```
If we set new `test.placeholder.value` to a variable it will be resolved to "*my string value 1*".
```java
// Will be "my string value 1"
public static final String PLACEHOLDER_VALUE = PROVIDER.getProperty("test.placeholder.value", String.class);
``` 
### Placeholder default value
### Recursive Placeholders
### Gradle
TBD
### Maven
TBD
## Motivation
TBD