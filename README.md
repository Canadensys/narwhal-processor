narwhal-processor
=================

narwhal-processor is a processing library that normalizes data of a known type. Current and proposed data types that can be normalized include date, country name, continent, state and province, coordinates, numeric range (altitude, depth) and scientific name.

Comments, contributions, reviews and help are welcomed.

Note
----
This library is still under active development. Some parts may change based on the reviews, comments and usage. Do not hesitate to enter an [Issue](https://github.com/Canadensys/narwhal-processor/issues) if you have any problems or questions.

Goal
----
The goal of this library is to provide a set of processing functions through a common Java interface that supports [JavaBeans](http://en.wikipedia.org/wiki/JavaBeans). This will ease the integration of the library in various biodiversity projects by providing a uniform way to access processing functions.

Scope
-----
The narwhal-processor is meant to be used as a low-level processing library with few secondary or contextual validations. For example, given a date such as 1999-01-16, the output (if successful) will be parsed into day (16), month (01), and year (1999). However, if this date represents the date of collection, it is out of scope to determine the biological validity of Jan 16, 1999.
The narwhal-processor only produces results from data that are without uncertainty.

Documentation and Usage
-----------------------
See our [wiki](https://github.com/Canadensys/narwhal-processor/wiki) for all the information.

Dependencies
------------
 * [GBIF Parsers](http://code.google.com/p/gbif-common-resources/) 0.2 (included by Maven)
 * [Apache Commons BeanUtils](http://commons.apache.org/beanutils/) 1.8.3 (included by Maven)
 * [canadensys-core](https://github.com/Canadensys/canadensys-core) 1.5 (included by Maven)
 * [JSR-310](http://threeten.sourceforge.net/) 0.6.3

To include JSR-310 into your Maven local repo
```
mvn install:install-file -DgroupId=threeten -DartifactId=threeten -Dversion=0.6.3 -Dpackaging=jar -Dfile=lib/threeten/threeten/0.6.3/threeten-0.6.3.jar
```

__Tested with Maven 3__

Build
-----
```
mvn package
```

Tests
-----
Unit tests
```
mvn test
```

Setup in Eclipse
----------------
After a git clone
```
mvn install:install-file -DgroupId=threeten -DartifactId=threeten -Dversion=0.6.3 -Dpackaging=jar -Dfile=lib/threeten/threeten/0.6.3/threeten-0.6.3.jar
mvn eclipse:eclipse
```
In Eclipse : File/Import/Existing Projects into Workspace

You may need to add the maven repository to Eclipse's Build Path via Preferences > Java > Build Path > Classpath Variables by clicking the New button and adding the name M2\_REPO\_ and the directory. On a Mac, this is usually /Users/<User>/.m2/repository.

