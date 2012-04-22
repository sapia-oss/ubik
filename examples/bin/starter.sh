#! /bin/sh

# Look for JAVA HOME environment variables
if [ -z "${JAVA_HOME}${JAVA_HOME_OVERRIDE}" ] ; then
    echo "ERROR: The variable JAVA_HOME is not define; cannot start."
    echo "       Please set your JAVA_HOME environment variable or use the -javahome parameter."
    exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*) cygwin=true ;;
    Darwin*) darwin=true ;;
esac


# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "${JAVA_HOME}" ] &&
        JAVA_HOME=`cygpath --unix "${JAVA_HOME}"`
    [ -n "${JAVA_HOME_OVERRIDE}" ] &&
        JAVA_HOME_OVERRIDE=`cygpath --unix "${JAVA_HOME_OVERRIDE}"`
    [ -n "${CLASSPATH}" ] &&
        CLASSPATH=`cygpath --path --unix "${CLASSPATH}"`
fi

if [ -n "${JAVA_HOME_OVERRIDE}" ] ; then
    if [ -x "${JAVA_HOME_OVERRIDE}/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=${JAVA_HOME_OVERRIDE}/jre/sh/java
    else
        JAVACMD=${JAVA_HOME_OVERRIDE}/bin/java
    fi
else
    if [ -x "${JAVA_HOME}/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=${JAVA_HOME}/jre/sh/java
    else
        JAVACMD=${JAVA_HOME}/bin/java
    fi
fi

if [ ! -x "${JAVACMD}" ] ; then
    echo "Error: JAVA_HOME is not defined correctly."
    echo "  We cannot execute ${JAVACMD}"
    exit 1
fi

MAINCLASS=org.sapia.ubik.rmi.server.ServerStarter

STARTER_CLASSPATH=""
for JAR in ../lib/*.jar
do
    # if the directory is empty, then it will return the input string
    # this is stupid, so case for it
    if [ "${JAR}" != "../lib/*.jar" ] ; then
        if [ -z "${STARTER_CLASSPATH}" ] ; then
            STARTER_CLASSPATH=${JAR}
        else
            STARTER_CLASSPATH="${JAR}":${STARTER_CLASSPATH}
        fi
    fi
done

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JAVA_HOME=`cygpath --path --windows "${JAVA_HOME}"`
    JAVA_HOME_OVERRIDE=`cygpath --path --windows "${JAVA_HOME_OVERRIDE}"`
    STARTER_CLASSPATH=`cygpath --path --windows "${STARTER_CLASSPATH}"`
fi

${JAVACMD} -cp ${STARTER_CLASSPATH} ${MAINCLASS} "$@"

