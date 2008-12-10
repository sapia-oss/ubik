@echo off

set classpath=../dist/sapia_ubik.jar;../lib/sapia_taskman.jar;../lib/jug.jar

java org.sapia.ubik.rmi.naming.remote.JNDIServer %1 %2 %3 %4

