package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServiceDiscoveryEg {
  public static void main(String[] args) {
    try {
      DiscoveryHelper helper = new DiscoveryHelper("default");

      System.out.println("Waiting for Service...");

      final ServiceDiscoListener listener = new ServiceDiscoListener() {
          public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
            System.out.println("Discovered: " + evt);
          }
        };

      helper.addServiceDiscoListener(listener);

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
