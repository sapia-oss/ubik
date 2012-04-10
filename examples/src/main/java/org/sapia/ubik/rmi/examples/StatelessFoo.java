package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.log.Log;
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
public class StatelessFoo implements Foo, Stateless, ServiceDiscoListener {
  public StatelessFoo() throws java.rmi.RemoteException {
  }

  /**
   * @see org.sapia.ubik.rmi.Foo#getBar()
   */
  public Bar getBar() throws RemoteException {
    return new StatelessUbikBar(hashCode());
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

      //PerfAnalyzer.getInstance().setEnabled(true);
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099/");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);

      ReliableLocalContext.currentContext().addServiceDiscoListener(new StatelessFoo());

      StatelessFoo foo = new StatelessFoo();
      ctx.rebind("Foo", foo);

      System.out.println("StatelessFoo started... @ " +
        Integer.toHexString(foo.hashCode()));

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
