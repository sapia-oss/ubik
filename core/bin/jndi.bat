echo off
cls

if not "%JAVA_HOME%"=="" goto okJavaHome

JAVA_HOME environment variable not set.
Set this variable to point to your Java
installation directory.

goto end

:okJavaHome

SET LOCALCLASSPATH=%CLASSPATH%;
if "%CLASSPATH%" == "" (
    SET LOCALCLASSPATH=
)
SET LOCALCLASSPATH_DEFINED=

if "%LOCALCLASSPATH_DEFINED%"=="true" goto okLcp

for %%i in (..\lib\*.jar) do call lcp.bat %%i
for %%i in (..\dist\*.jar) do call lcp.bat %%i

set LOCALCLASSPATH_DEFINED=true

:okLcp

set JAVACMD=%JAVA_HOME%\bin\java

SET APPCLASSPATH=%LOCALCLASSPATH%;%UBIK_CP%
if "%UBIK_CP%" == "" (
    set APPCLASSPATH=%LOCALCLASSPATH%
)

SET MAIN_CLASS=org.sapia.ubik.rmi.naming.remote.JNDIServer

"%JAVACMD%" %UBIK_OPTS% -classpath "%APPCLASSPATH%" %MAIN_CLASS% %*

if errorlevel 1 (
    echo "%JAVACMD%" %UBIK_OPTS% -classpath "%APPCLASSPATH%" %MAIN_CLASS% %*
    pause
)

:end
