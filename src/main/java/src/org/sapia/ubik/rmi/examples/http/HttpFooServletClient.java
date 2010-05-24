package org.sapia.ubik.rmi.examples.http;

import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.http.servlet.ServletAddress;
import org.sapia.ubik.rmi.server.transport.http.servlet.ServletTransportProvider;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpFooServletClient {
  public static void main(String[] args) {
    try {
      TransportManager.registerProvider(new ServletTransportProvider());

      Foo foo = (Foo) Hub.connect(new ServletAddress(
            "http://localhost:8080/ubik"));
      System.out.println(foo.getBar().getMsg());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
