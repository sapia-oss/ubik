package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Log;

import java.util.Properties;

import javax.naming.InitialContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JndiAttributesLookupEg {
  public static void main(String[] args) {
    try {
      Log.setWarning();

      //PerfAnalyzer.getInstance().setEnabled(true);
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      //props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "yan");
      InitialContext ctx = new InitialContext(props);

      Foo            f = (Foo) ctx.lookup("some/Foo?attr1=value1");

      System.out.println("ReliableFoo acquired...");

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
