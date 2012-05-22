echo off
cls

if not "%JAVA_HOME%"=="" goto okJavaHome

JAVA_HOME environment variable not set.
Set this variable to point to your Java
installation directory.

goto end

:okJavaHome

SET LOCALCLASSPATH=

if "%LOCALCLASSPATH_DEFINED%"=="true" goto okLcp

for %%i in (..\lib\*.jar) do call lcp.bat %%i
for %%i in (..\dist\*.jar) do call lcp.bat %%i

set LOCALCLASSPATH_DEFINED=true

:okLcp

rem echo %LOCALCLASSPATH%

set APPCLASSPATH=%CLASSPATH%;%LOCALCLASSPATH%;%UBIK_CP%

"%JAVA_HOME%\bin\java" %UBIK_OPTS% -classpath "%APPCLASSPATH%" org.sapia.ubik.rmi.server.ServerStarter %*

:end
