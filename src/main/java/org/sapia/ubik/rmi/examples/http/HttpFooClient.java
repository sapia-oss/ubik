package org.sapia.ubik.rmi.examples.http;

import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpFooClient {
  public static void main(String[] args) {
    try {
      Hub.getModules().getTransportManager().registerProvider(new HttpTransportProvider());

      Foo foo = (Foo) Hub.connect(new HttpAddress(Uri.parse(
              "http://localhost:8080/ubik")));
      
      // If Ubik server is behind a firewall 
      // at www.somedomain.net, connect to it using
      // as follows (assuming port is 8181; change according
      // to your config)
			/*Foo foo = (Foo) Hub.connect(new HttpAddress(Uri.parse(
							"http://www.somedomain.net:8181/ubik")));*/              
			              
      System.out.println("1- " + foo.getBar().getMsg());
      System.out.println("2- " + foo.getBar().getMsg());
			System.out.println("3- " + foo.getBar().getMsg());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
