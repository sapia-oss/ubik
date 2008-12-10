@echo off


set classpath=../dist/sapia_ubik.jar;../lib/sapia_taskman.jar;../lib/channel.jar

echo %classpath%

start "Jdk Foo" java org.sapia.ubik.rmi.examples.JdkFoo

start "Ubik Foo" java org.sapia.ubik.rmi.examples.UbikFoo



