@echo off

set classpath=../dist/sapia_ubik.jar;../lib/jug.jar;../lib/sapia_taskman.jar

echo %classpath%

java org.sapia.ubik.rmi.examples.UbikFoo

pause



