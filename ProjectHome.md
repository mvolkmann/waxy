WAX is a new approach to writing XML that
  * focuses on writing XML, not reading it
  * requires less code than other approaches
  * uses less memory than other approaches (because it outputs XML as each method is called rather than storing it in a DOM-like structure and outputting it later)
  * doesn't depend on any Java classes other than standard JDK classes
  * is a small library (around 12K)
  * writes all XML node types
  * always outputs well-formed XML or throws an exception
  * provides extensive error checking
  * automatically escapes special characters in text and attribute values when error checking is turned on
  * allows all error checking to be turned off for performance
  * knows how to associate DTDs, XML Schemas and XSLT stylesheets with the XML it outputs
  * is well-suited for writing XML request and response messages for REST-based and SOAP-based services

For more information, see the home page which includes usage examples.

The version of WAX for Java 1.4 requires retroweaver-rt-{version}.jar
which is available under BSD license from http://retroweaver.sourceforge.net/.