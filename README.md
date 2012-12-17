narwhal-processor
=================

Basic data processing library aiming to normalize similar values ​​in a known format.

Comments, contributions, reviews and help are welcomed.

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