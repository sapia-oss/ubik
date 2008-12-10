@echo off


set classpath=../dist/sapia_ubik.jar;../lib/sapia_taskman.jar;../lib/jug.jar

echo %classpath%

java org.sapia.ubik.rmi.examples.Bench 

echo again...

java org.sapia.ubik.rmi.examples.Bench 

pause


