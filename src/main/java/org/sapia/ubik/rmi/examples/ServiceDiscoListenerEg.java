package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;

import java.util.*;

import javax.naming.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServiceDiscoListenerEg implements ServiceDiscoListener {
  /**
   * Constructor for ServiceDiscoListenerExample.
   */
  public ServiceDiscoListenerEg() {
    super();
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener#onServiceDiscovered(ServiceDiscoveryEvent)
   */
  public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
    System.out.println("Discovered: " + evt);
  }

  public static void main(String[] args) {
    System.out.println("Waiting for service discovery. Press CTRL-C to stop.");

    try {
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1098/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext         ctx = new InitialContext(props);

      ServiceDiscoListenerEg sdisco = new ServiceDiscoListenerEg();

      ReliableLocalContext   rctx = ReliableLocalContext.currentContext();
      rctx.addServiceDiscoListener(sdisco);

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
