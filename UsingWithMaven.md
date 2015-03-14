# WAX in your Maven project #

WAX is not yet available to Maven central. To use it in your Maven 2 build:

  * Download a JAR from http://code.google.com/p/waxy/downloads/list
  * Install in your local repository using the following command line:
`mvn install:install-file -DgroupId=com.ociweb.xml -DartifactId=wax -Dversion=1.0.5 -Dpackaging=jar -Dfile=/path/to/file`
> > Make sure to change the path so that it points to the JAR file you downloaded.
  * Add the following dependency to your project's pom.xml (change the version if needed):
```
<dependency>
   <groupId>com.ociweb.xml</groupId>
   <artifactId>wax</artifactId>
   <version>1.0.5</version>
</dependency>
```