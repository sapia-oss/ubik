THIS PROJECT IS MADE PUBLIC UNDER  THE SAPIA-OSS
LICENSE. SEE LICENSE.txt THAT COMES  PACKAGED IN 
THE ROOT OF THE DISTRIBUTION OR GO TO 

http://www.sapia-oss.org/license.html 


SAPIA UBIK
==========

Ubik is a distributed computing framework that includes
a RMI-like API suited for implementing reliable, robust, 
scalable distributed services.

** This product includes software developed  by the      **
** Apache Software Foundation  (http://www.apache.org/). ** 



FILES
=====

./LICENSE.txt               Sapia license file


DEPENDENCIES
============

Junit 3.7.               (http://www.junit.org/index.htm)

Simple                   (http://simpleweb.sf.net)

Jetty                    (http://jetty.mortbay.org/jetty/index.html)

Servlets                 (http://java.sun.com)

Http client              (http://jakarta.apache.org/commons/httpclient/)

Mina                     (http://mina.apache.org/)

Slf4j (used by Mina)     (http://www.slf4j.org/)

Notes:

* You do not need Junit in your classpath at runtime.
* The jars required by the HTTP transport layer are under the
  lib/http directory.
* Jetty is used to run/compile an example. You do not need its 
  library at runtime.
* The servlet API jar is not needed in your classpath at runtime 
  (the servlet container has it).
* The Simple jar is not needed in your classpath at runtime
  if your are not using the HTTP stand-alone transport (or, more specifically,
  the HttpTransportProvider - see documentation). 
* If your are using the HTTP transports, its is recommended that 
  you use Jakarta's HTTP client (put it in your classpath at runtime).
  If not present, Ubik falls back to the JDK's HttpUrlConnection.
* Ubik's NIO support is based on Mina. The NIO support is not quite ready
  for primetime. You need Mina in order to compile the source.


SUPPORT
=======

Mailing List: sapia-ubik_users@lists.sourceforge.net

Email: info@sapia-oss.org


INSTALLATION
============

1. Download

Download the distribution from SourceForge 
(http://www.sourceforge.net/projects/sapia). The download
link appears in the "Files" section; the latest "ubik"
release should be downloaded.


2. Extract

Extract the distribution under a directory of your choice
on the target computer.


3. Set-up Environment

The library itself can be found under the ./dist directory
of the extraction directory. The project requires the libraries
under the ./lib directory as dependencies, so put this project's 
jar file, as well as the jars that appear in the ./lib directory, 
in your application's classpath.


BUILD
=====

You must have Ant installed (see http://ant.apache.org).

In this directory, type the "ant" command, followed by the
name of the target you want to execute (example: to compile
the sources, type "ant compile".

The following targets are available:

- init

 Creates the directories (under this directory) necessary
 to build this project.

 THIS TARGET MUST BE CALLED THE FIRST TIME THIS PROJECT 
 IS BUILT.

- compile

 Compiles the sources.

- test

 Runs the unit tests.

- dist
 
 Creates the library for this project (in the ./dist
 directory).

