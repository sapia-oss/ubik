#!/bin/sh

$JAVA_HOME/bin/java -cp ../conf/:./run.jar:../lib/log4j-1.2.8.jar:../lib/jms.jar:../../dist/sapia_ubik.jar:../../lib/jug.jar:../../lib/sapia_archie.jar:../../lib/sapia_taskman.jar org.ejtools.jndi.browser.Main
