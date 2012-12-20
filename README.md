narwhal-processor
=================

Basic data processing library aiming to normalize similar values ​​in a known format. The value can be standardized or discovered from another value.

Comments, contributions, reviews and help are welcomed.

Note
----
This library is still young and under active development. Some parts may change based on the reviews, comments and usage. Do not hesitate to enter an [Issue](https://github.com/Canadensys/narwhal-processor/issues) if you have any problems or questions.

Documentation
-------------
See [wiki](https://github.com/Canadensys/narwhal-processor/wiki)

Dependencies
------------
[JSR-310](http://threeten.sourceforge.net/) 0.6.3

To include it into your Maven local repo
```
mvn install:install-file -DgroupId=threeten -DartifactId=threeten -Dversion=0.6.3 -Dpackaging=jar -Dfile=lib/threeten/threeten/0.6.3/threeten-0.6.3.jar
```

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