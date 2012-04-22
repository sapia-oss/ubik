package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.remote.JNDIServer;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;
import org.sapia.ubik.rmi.server.Stateless;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ColocatedStatelessFoo implements Foo, Stateless,
  ServiceDiscoListener {
  public ColocatedStatelessFoo() throws java.rmi.RemoteException {
  }

  /**
   * @see org.sapia.ubik.rmi.Foo#getBar()
   */
  public Bar getBar() throws RemoteException {
    return new UbikBar();
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener#onServiceDiscovered(ServiceDiscoveryEvent)
   */
  public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
    System.out.println("Discovered : " + evt);
  }

  public static void main(String[] args) {
    try {
      Log.setWarning();

      Thread t = new Thread() {
          public void run() {
          	try {
          		JNDIServer.main(new String[] {  });
          	} catch (Exception e) {
          		e.printStackTrace();
          	}
          }
        };

      t.start();
      Thread.sleep(2000);

      //PerfAnalyzer.getInstance().setEnabled(true);
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);

      ReliableLocalContext.currentContext().addServiceDiscoListener(new ColocatedStatelessFoo());

      ctx.rebind("Foo", new ColocatedStatelessFoo());

      System.out.println("StatelessFoo started...");

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
