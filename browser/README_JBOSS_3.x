# ------------------------------------------------------------
#
# EJTools, the Enterprise Java Tools
#
# Distributable under LGPL license.
# See terms of license at www.gnu.org.
#
# Feedback and support at http://sourceforge.net/project/ejtools
#
# ------------------------------------------------------------

To use the Swing based JNDI Browser under JBoss 3.x, do as follow :
- copy the jbossall-client.jar under the client folder of the JBoss installation ($JBOSS_HOME/client or %JBOSS_HOME%\client) to the lib/ext folder of JNDI Browser.
- copy the jcert.jar, jnet.jar and jsse.jar fiels under the client folder of the JBoss installation ($JBOSS_HOME/client or %JBOSS_HOME%\client) to the lib/ext folder of JMX Browser, if you are using JDK1.3
- copy the jboss-jmx.jar file under the lib folder of the JBoss installation ($JBOSS_HOME/lib or %JBOSS_HOME%\lib) to the lib/ext folder of JNDI Browser.

That's it.
