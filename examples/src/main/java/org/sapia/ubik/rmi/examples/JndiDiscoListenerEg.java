package org.sapia.ubik.rmi.examples;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JndiDiscoListenerEg implements JndiDiscoListener {
  /**
   * Constructor for ServiceDiscoListenerExample.
   */
  public JndiDiscoListenerEg() {
    super();
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener#onJndiDiscovered(Context)
   */
  public void onJndiDiscovered(Context ctx) {
    System.out.println("JNDI discovered!!!!");
  }

  public static void main(String[] args) {
    try {
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext       ctx = new InitialContext(props);

      JndiDiscoListenerEg  sdisco = new JndiDiscoListenerEg();

      ReliableLocalContext rctx = ReliableLocalContext.currentContext();
      rctx.addJndiDiscoListener(sdisco);

      System.out.println("Waiting for jndi discovery. Press CTRL-C to stop.");

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
